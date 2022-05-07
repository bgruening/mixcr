package com.milaboratory.mixcr.cli;

import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.cli.BinaryFileInfo;
import com.milaboratory.cli.PipelineConfiguration;
import com.milaboratory.mixcr.assembler.CloneAssemblerParameters;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.milaboratory.mixcr.basictypes.*;
import com.milaboratory.mixcr.postanalysis.*;
import com.milaboratory.mixcr.postanalysis.additive.AAProperties;
import com.milaboratory.mixcr.postanalysis.additive.AdditiveCharacteristics;
import com.milaboratory.mixcr.postanalysis.additive.KeyFunctions;
import com.milaboratory.mixcr.postanalysis.diversity.DiversityCharacteristic;
import com.milaboratory.mixcr.postanalysis.diversity.DiversityMeasure;
import com.milaboratory.mixcr.postanalysis.downsampling.ClonesDownsamplingPreprocessorFactory;
import com.milaboratory.mixcr.postanalysis.downsampling.DownsampleValueChooser;
import com.milaboratory.mixcr.postanalysis.overlap.*;
import com.milaboratory.mixcr.postanalysis.preproc.ElementPredicate;
import com.milaboratory.mixcr.postanalysis.preproc.NoPreprocessing;
import com.milaboratory.mixcr.postanalysis.preproc.OverlapPreprocessorAdapter;
import com.milaboratory.mixcr.postanalysis.preproc.SelectTop;
import com.milaboratory.mixcr.postanalysis.spectratype.SpectratypeCharacteristic;
import com.milaboratory.mixcr.postanalysis.spectratype.SpectratypeKeyFunction;
import com.milaboratory.mixcr.postanalysis.ui.*;
import com.milaboratory.mixcr.vdjaligners.VDJCAlignerParameters;
import com.milaboratory.util.GlobalObjectMappers;
import com.milaboratory.util.LambdaSemaphore;
import com.milaboratory.util.SmartProgressReporter;
import io.repseq.core.*;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.milaboratory.mixcr.postanalysis.additive.AdditiveCharacteristics.*;
import static io.repseq.core.Chains.*;
import static java.util.stream.Collectors.*;

/**
 *
 */
public abstract class CommandPa extends ACommandWithOutputMiXCR {
    public static final NamedChains[] CHAINS = {TRAD_NAMED, TRB_NAMED, TRG_NAMED, IGH_NAMED, IGKL_NAMED};

    @Parameters(description = "cloneset.{clns|clna}... result.json")
    public List<String> inOut;

    @Option(description = "Use only productive sequences in postanalysis.",
            names = {"--only-productive"})
    public boolean onlyProductive = false; // FIXME - not used

    @Option(description = "Choose downsampling. Possible values: umi-count-[1000|auto]|cumulative-top-[percent]|top-[number]|no-downsampling",
            names = {"-d", "--downsampling"},
            required = true)
    public String downsampling;

    @Option(description = "Chains",
            names = {"-c", "--chains"})
    public String chains = "ALL";

    @Override
    protected List<String> getInputFiles() {
        return inOut.subList(0, inOut.size() - 1)
                .stream()
                .flatMap(f -> {
                    if (Files.isDirectory(Paths.get(f))) {
                        try {
                            return Files
                                    .list(Paths.get(f))
                                    .map(Path::toString);
                        } catch (IOException ignored) {
                        }
                    }
                    return Stream.of(f);
                })
                .collect(toList());
    }

    @Override
    protected List<String> getOutputFiles() {
        return Collections.singletonList(outputFile());
    }

    String outputFile() {
        return inOut.get(inOut.size() - 1);
    }

    private static int downsamplingValue(String downsampling) {
        return Integer.parseInt(downsampling.substring(downsampling.lastIndexOf("-") + 1));
    }

    /**
     * Get sample id from file name
     */
    static String getSampleId(String file) {
        return Paths.get(file).getFileName().toString();
    }

    private static SetPreprocessorFactory<Clone> parseDownsampling(String downsampling) {
        if (downsampling.equalsIgnoreCase("no-downsampling")) {
            return new NoPreprocessing.Factory<>();
        } else if (downsampling.startsWith("umi-count")) {
            if (downsampling.endsWith("auto"))
                return new ClonesDownsamplingPreprocessorFactory(new DownsampleValueChooser.Auto(), 314);
            else {
                return new ClonesDownsamplingPreprocessorFactory(new DownsampleValueChooser.Fixed(downsamplingValue(downsampling)), 314);
            }
        } else {
            int value = downsamplingValue(downsampling);
            if (downsampling.startsWith("cumulative-top")) {
                return new SelectTop.Factory<>(WeightFunctions.Count, 1.0 * value / 100.0);
            } else if (downsampling.startsWith("top")) {
                return new SelectTop.Factory<>(WeightFunctions.Count, value);
            } else {
                throw new IllegalArgumentException("Illegal downsampling string: " + downsampling);
            }
        }
    }

    SetPreprocessorFactory<Clone> downsampling() {
        return downsampling(this.downsampling);
    }

    SetPreprocessorFactory<Clone> downsampling(String downsamplingStr) {
        SetPreprocessorFactory<Clone> downsampling =
                parseDownsampling(downsamplingStr);

        if (onlyProductive) {
            List<ElementPredicate<Clone>> filters = new ArrayList<>();
            filters.add(new ElementPredicate.NoStops(GeneFeature.CDR3));
            filters.add(new ElementPredicate.NoOutOfFrames(GeneFeature.CDR3));
            downsampling = downsampling.filterFirst(filters);
        }

        return downsampling;
    }

    /**
     * Resulting data written to disk
     */
    @JsonAutoDetect
    public static final class PaResult {
        /**
         * Results for individual chains
         */
        @JsonProperty("results")
        @JsonSerialize(keyUsing = KnownChainsKeySerializer.class)
        @JsonDeserialize(keyUsing = KnownChainsKeyDeserializer.class)
        public final Map<NamedChains, PaResultByChain> results;

        @JsonCreator
        public PaResult(@JsonProperty("results") Map<NamedChains, PaResultByChain> results) {
            this.results = results;
        }
    }

    /**
     * Resulting data written to disk
     */
    @JsonAutoDetect
    public static final class PaResultByChain {
        @JsonProperty("schema")
        public final PostanalysisSchema<?> schema;
        @JsonProperty("result")
        public final PostanalysisResult result;

        @JsonCreator
        public PaResultByChain(@JsonProperty("schema") PostanalysisSchema<?> schema,
                               @JsonProperty("result") PostanalysisResult result) {
            this.schema = schema;
            this.result = result;
        }
    }

    @Override
    public void run0() throws Exception {
        Map<NamedChains, PaResultByChain> resultsMap = new HashMap<>();
        Chains c = Chains.parse(chains);
        for (NamedChains knownChains : CHAINS) {
            if (c.intersects(knownChains.chains))
                resultsMap.put(knownChains, run(knownChains.chains));
        }
        PaResult result = new PaResult(resultsMap);
        try {
            GlobalObjectMappers.PRETTY.writeValue(new File(outputFile()), result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    abstract PaResultByChain run(Chains chain);

    ///////////////////////////////////////////// Individual /////////////////////////////////////////////

    static final String
            Biophysics = "biophysics",
            Diversity = "diversity",
            VUsage = "vUsage",
            JUsage = "JUsage",
            VJUsage = "VJUsage",
            IsotypeUsage = "IsotypeUsage",
            CDR3Spectratype = "CDR3Spectratype",
            VSpectratype = "VSpectratype",
            VSpectratypeMean = "VSpectratypeMean",
            Overlap = "overlap";

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    @CommandLine.Command(name = "individual",
            sortOptions = false,
            separator = " ",
            description = "Biophysics, Diversity, V/J/VJ-Usage, CDR3/V-Spectratype")
    public static class CommandIndividual extends CommandPa {
        public CommandIndividual() {}

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        PaResultByChain run(Chains chain) {
            List<CharacteristicGroup<?, Clone>> groups = new ArrayList<>();

            SetPreprocessorFactory<Clone> downsampling = downsampling()
                    .filterFirst(new ElementPredicate.IncludeChains(chain));

            groups.add(new CharacteristicGroup<>(Biophysics,
                    Arrays.asList(
                            weightedLengthOf(downsampling, GeneFeature.CDR3, false).setName("CDR3 length, nt"),
                            weightedLengthOf(downsampling, GeneFeature.CDR3, true).setName("CDR3 length, aa"),
                            weightedLengthOf(downsampling, GeneFeature.VJJunction, false).setName("NDN length, nt"),
                            weightedAddedNucleotides(downsampling).setName("Added N, nt"),
                            weightedBiophysics(downsampling, AAProperties.AAProperty.N2Strength, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Strength"),
                            weightedBiophysics(downsampling, AAProperties.AAProperty.N2Hydrophobicity, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Hydrophobicity"),
                            weightedBiophysics(downsampling, AAProperties.AAProperty.N2Surface, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Surface"),
                            weightedBiophysics(downsampling, AAProperties.AAProperty.N2Volume, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Volume"),
                            weightedBiophysics(downsampling, AAProperties.AAProperty.Charge, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Charge")
                    ),
                    Arrays.asList(new GroupSummary<>())
            ));

            groups.add(new CharacteristicGroup<>(Diversity,
                    Arrays.asList(new DiversityCharacteristic<>("Diversity", new WeightFunctions.Count(),
                            downsampling,
                            new DiversityMeasure[]{
                                    DiversityMeasure.Observed,
                                    DiversityMeasure.Clonality,
                                    DiversityMeasure.ShannonWeiner,
                                    DiversityMeasure.InverseSimpson,
                                    DiversityMeasure.Chao1,
                                    DiversityMeasure.Gini
                            })),
                    Arrays.asList(new GroupSummary<>())
            ));

            groups.add(new CharacteristicGroup<>(VUsage,
                    Arrays.asList(AdditiveCharacteristics.segmentUsage(downsampling, GeneType.Variable)),
                    Arrays.asList(new GroupSummary<>())
            ));
            groups.add(new CharacteristicGroup<>(JUsage,
                    Arrays.asList(AdditiveCharacteristics.segmentUsage(downsampling, GeneType.Joining)),
                    Arrays.asList(new GroupSummary<>())
            ));
            groups.add(new CharacteristicGroup<>(VJUsage,
                    Arrays.asList(AdditiveCharacteristics.vjSegmentUsage(downsampling)),
                    Arrays.asList(new GroupSummary<>(), new GroupMelt.VJUsageMelt<>())
            ));

            groups.add(new CharacteristicGroup<>(IsotypeUsage,
                    Arrays.asList(AdditiveCharacteristics.isotypeUsage(downsampling)),
                    Arrays.asList(new GroupSummary<>())
            ));

            groups.add(new CharacteristicGroup<>(CDR3Spectratype,
                    Arrays.asList(new SpectratypeCharacteristic("CDR3 spectratype",
                            downsampling, 10,
                            new SpectratypeKeyFunction<>(new KeyFunctions.AAFeature(GeneFeature.CDR3), GeneFeature.CDR3, false))),
                    Collections.singletonList(new GroupSummary<>())));

            groups.add(new CharacteristicGroup<>(VSpectratype,
                    Arrays.asList(AdditiveCharacteristics.VSpectratype(downsampling)),
                    Collections.singletonList(new GroupSummary<>())));

            groups.add(new CharacteristicGroup<>(VSpectratypeMean,
                    Arrays.asList(AdditiveCharacteristics.VSpectratypeMean(downsampling)),
                    Collections.singletonList(new GroupSummary<>())));

            PostanalysisSchema<Clone> schema = new PostanalysisSchema<>(groups);
            PostanalysisRunner runner = new PostanalysisRunner<>();
            runner.addCharacteristics(schema.getAllCharacterisitcs());

            List<Dataset> datasets = getInputFiles().stream()
                    .map(file ->
                            new ClonotypeDataset(getSampleId(file), file, VDJCLibraryRegistry.getDefault())
                    ).collect(Collectors.toList());

            System.out.println("Running for " + chain);
            SmartProgressReporter.startProgressReport(runner);
            return new PaResultByChain(schema, runner.run(datasets));
        }
    }

    ///////////////////////////////////////////// Overlap /////////////////////////////////////////////

    @CommandLine.Command(name = "overlap",
            sortOptions = false,
            separator = " ",
            description = "Overlap analysis")
    public static class CommandOverlap extends CommandPa {
        @Option(description = "Override downsampling for F2 umi|d[number]|f[number]",
                names = {"--f2-downsampling"},
                required = false)
        public String f2downsampling;

        public CommandOverlap() {
        }

        @Override
        @SuppressWarnings("unchecked")
        PaResultByChain run(Chains chain) {
            SetPreprocessorFactory<Clone> downsampling = downsampling();

            Map<OverlapType, SetPreprocessorFactory<Clone>> downsamplingByType = new HashMap<>();
            downsamplingByType.put(OverlapType.D, downsampling);
            downsamplingByType.put(OverlapType.F2, f2downsampling == null
                    ? downsampling
                    : downsampling(f2downsampling));
            downsamplingByType.put(OverlapType.R_Intersection, downsampling);

            List<VDJCSProperties.VDJCSProperty<VDJCObject>> ordering = VDJCSProperties.orderingByAminoAcid(new GeneFeature[]{GeneFeature.CDR3});
            OverlapPostanalysisSettings overlapPA = new OverlapPostanalysisSettings(
                    ordering,
                    new WeightFunctions.Count(),
                    downsamplingByType
            );

            PostanalysisSchema<OverlapGroup<Clone>> schema = overlapPA.getSchema(getInputFiles().size(), chain);

            // Limits concurrency across all readers
            LambdaSemaphore concurrencyLimiter = new LambdaSemaphore(32);
            List<CloneReader> readers = getInputFiles()
                    .stream()
                    .map(s -> {
                        try {
                            return mkCheckedReader(
                                    Paths.get(s).toAbsolutePath(),
                                    concurrencyLimiter);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            OverlapDataset<Clone> overlapDataset = OverlapUtil.overlap(
                    getInputFiles().stream().map(CommandPa::getSampleId).collect(toList()),
                    ordering,
                    readers);

            PostanalysisRunner<OverlapGroup<Clone>> runner = new PostanalysisRunner<>();
            runner.addCharacteristics(schema.getAllCharacterisitcs());

            System.out.println("Running for " + chain);
            SmartProgressReporter.startProgressReport(runner);
            PostanalysisResult result = runner.run(overlapDataset);

            return new PaResultByChain(schema, result);
        }

        public static CloneReader mkCheckedReader(Path path,
                                                  LambdaSemaphore concurrencyLimiter) throws IOException {
            ClnsReader inner = new ClnsReader(
                    path,
                    VDJCLibraryRegistry.getDefault(),
                    concurrencyLimiter);
            return new CloneReader() {
                @Override
                public PipelineConfiguration getPipelineConfiguration() {
                    return inner.getPipelineConfiguration();
                }

                @Override
                public PipelineConfiguration fromFileOrNull(String fileName, BinaryFileInfo fileInfo) {
                    return inner.fromFileOrNull(fileName, fileInfo);
                }

                @Override
                public PipelineConfiguration fromFile(String fileName) {
                    return inner.fromFile(fileName);
                }

                @Override
                public PipelineConfiguration fromFile(String fileName, BinaryFileInfo fileInfo) {
                    return inner.fromFile(fileName, fileInfo);
                }

                @Override
                public VDJCAlignerParameters getAlignerParameters() {
                    return inner.getAlignerParameters();
                }

                @Override
                public CloneAssemblerParameters getAssemblerParameters() {
                    return inner.getAssemblerParameters();
                }

                @Override
                public List<VDJCGene> getGenes() {
                    return inner.getGenes();
                }

                @Override
                public VDJCSProperties.CloneOrdering ordering() {
                    return inner.ordering();
                }

                @Override
                public OutputPortCloseable<Clone> readClones() {
                    OutputPortCloseable<Clone> in = inner.readClones();
                    return new OutputPortCloseable<Clone>() {
                        @Override
                        public void close() {
                            in.close();
                        }

                        @Override
                        public Clone take() {
                            Clone t = in.take();
                            if (t == null)
                                return null;
                            if (t.getFeature(GeneFeature.CDR3) == null)
                                return take();
                            return t;
                        }
                    };
                }

                @Override
                public void close() throws Exception {
                    inner.close();
                }

                @Override
                public int numberOfClones() {
                    return inner.numberOfClones();
                }
            };
        }

        @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
        static final class OverlapPostanalysisSettings {
            final List<VDJCSProperties.VDJCSProperty<VDJCObject>> ordering;
            final WeightFunction<Clone> weight;
            final Map<OverlapType, SetPreprocessorFactory<Clone>> preprocessors;
            final Map<SetPreprocessorFactory<Clone>, List<OverlapType>> groupped;

            OverlapPostanalysisSettings(List<VDJCSProperties.VDJCSProperty<VDJCObject>> ordering,
                                        WeightFunction<Clone> weight,
                                        Map<OverlapType, SetPreprocessorFactory<Clone>> preprocessors) {
                this.ordering = ordering;
                this.weight = weight;
                this.preprocessors = preprocessors;
                this.groupped = preprocessors.entrySet().stream().collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toList())));
            }

            private SetPreprocessorFactory<OverlapGroup<Clone>> getPreprocessor(OverlapType type, Chains chain) {
                return new OverlapPreprocessorAdapter.Factory<>(preprocessors.get(type).filterFirst(new ElementPredicate.IncludeChains(chain)));
            }

            //fixme only productive??
            public List<OverlapCharacteristic<Clone>> getCharacteristics(int i, int j, Chains chain) {
                return groupped.entrySet().stream().map(e -> new OverlapCharacteristic<>("overlap_" + i + "_" + j + " / " + e.getValue().stream().map(t -> t.name).collect(Collectors.joining(" / ")), weight,
                        new OverlapPreprocessorAdapter.Factory<>(e.getKey().filterFirst(new ElementPredicate.IncludeChains(chain))),
                        e.getValue().toArray(new OverlapType[0]),
                        i, j)).collect(toList());
            }

            public PostanalysisSchema<OverlapGroup<Clone>> getSchema(int nSamples, Chains chain) {
                List<OverlapCharacteristic<Clone>> overlaps = new ArrayList<>();
                for (int i = 0; i < nSamples; ++i)
                    for (int j = i + 1; j < nSamples; ++j)
                        overlaps.addAll(getCharacteristics(i, j, chain));

                return new PostanalysisSchema<>(Collections.singletonList(
                        new CharacteristicGroup<>(Overlap,
                                overlaps,
                                Arrays.asList(new OverlapSummary<>())
                        )));
            }
        }
    }

    @CommandLine.Command(name = "postanalysis",
            separator = " ",
            description = "Run postanalysis routines.",
            subcommands = {
                    CommandLine.HelpCommand.class
            })
    public static class CommandPostanalysisMain {
    }
}