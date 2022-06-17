/*
 * Copyright (c) 2014-2022, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/mixcr/blob/develop/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
package com.milaboratory.mixcr.postanalysis.ui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.milaboratory.mixcr.basictypes.Clone;
import com.milaboratory.mixcr.basictypes.tag.TagsInfo;
import com.milaboratory.mixcr.postanalysis.Characteristic;
import com.milaboratory.mixcr.postanalysis.WeightFunctions;
import com.milaboratory.mixcr.postanalysis.additive.AAProperties;
import com.milaboratory.mixcr.postanalysis.additive.AdditiveCharacteristics;
import com.milaboratory.mixcr.postanalysis.additive.KeyFunctions;
import com.milaboratory.mixcr.postanalysis.diversity.DiversityCharacteristic;
import com.milaboratory.mixcr.postanalysis.diversity.DiversityMeasure;
import com.milaboratory.mixcr.postanalysis.preproc.SelectTop;
import com.milaboratory.mixcr.postanalysis.spectratype.SpectratypeCharacteristic;
import com.milaboratory.mixcr.postanalysis.spectratype.SpectratypeKeyFunction;
import io.repseq.core.GeneFeature;
import io.repseq.core.GeneType;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mixcr.postanalysis.additive.AdditiveCharacteristics.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class PostanalysisParametersIndividual extends PostanalysisParameters {
    public static final String
            Biophysics = "biophysics",
            Diversity = "diversity",
            VUsage = "vUsage",
            JUsage = "JUsage",
            VJUsage = "VJUsage",
            IsotypeUsage = "IsotypeUsage",
            CDR3Spectratype = "CDR3Spectratype",
            VSpectratype = "VSpectratype",
            VSpectratypeMean = "VSpectratypeMean";

    public BiophysicsParameters biophysics = new BiophysicsParameters();
    public DiversityParameters diversity = new DiversityParameters();
    public MetricParameters vUsage = new MetricParameters();
    public MetricParameters jUsage = new MetricParameters();
    public MetricParameters vjUsage = new MetricParameters();
    public MetricParameters isotypeUsage = new MetricParameters();
    public MetricParameters cdr3Spectratype = new MetricParameters();
    public MetricParameters vSpectratype = new MetricParameters();
    public MetricParameters vSpectratypeMean = new MetricParameters();

    public List<CharacteristicGroup<?, Clone>> getGroups(TagsInfo tagsInfo) {
        for (WithParentAndTags wpt : Arrays.asList(
                biophysics, diversity, vUsage, jUsage, vjUsage, isotypeUsage, cdr3Spectratype, vSpectratype, vSpectratypeMean
        )) {
            wpt.setParent(this);
            wpt.setTagsInfo(tagsInfo);
        }

        return Arrays.asList(
                biophysics.getGroup(),
                diversity.getGroup(),
                new CharacteristicGroup<>(VUsage,
                        Arrays.asList(AdditiveCharacteristics.segmentUsage(
                                vUsage.preproc(),
                                vUsage.weightFunction(),
                                GeneType.Variable)),
                        Arrays.asList(new GroupSummary.Simple<>())
                ),

                new CharacteristicGroup<>(JUsage,
                        Arrays.asList(AdditiveCharacteristics.segmentUsage(
                                jUsage.preproc(),
                                jUsage.weightFunction(),
                                GeneType.Joining
                        )),
                        Arrays.asList(new GroupSummary.Simple<>())
                ),

                new CharacteristicGroup<>(VJUsage,
                        Arrays.asList(AdditiveCharacteristics.vjSegmentUsage(
                                vjUsage.preproc(),
                                vjUsage.weightFunction()
                        )),
                        Arrays.asList(new GroupSummary.VJUsage<>())
                ),

                new CharacteristicGroup<>(IsotypeUsage,
                        Arrays.asList(AdditiveCharacteristics.isotypeUsage(
                                isotypeUsage.preproc(),
                                isotypeUsage.weightFunction()
                        )),
                        Arrays.asList(new GroupSummary.Simple<>())
                ),

                new CharacteristicGroup<>(CDR3Spectratype,
                        Arrays.asList(new SpectratypeCharacteristic("CDR3 spectratype",
                                cdr3Spectratype.preproc(),
                                cdr3Spectratype.weightFunction(),
                                10,
                                new SpectratypeKeyFunction<>(new KeyFunctions.AAFeature(GeneFeature.CDR3), GeneFeature.CDR3, false))),
                        Collections.singletonList(new GroupSummary.Simple<>())),

                new CharacteristicGroup<>(VSpectratype,
                        Arrays.asList(AdditiveCharacteristics.VSpectratype(
                                vSpectratype.preproc(),
                                vSpectratype.weightFunction())),
                        Collections.singletonList(new GroupSummary.Simple<>())),

                new CharacteristicGroup<>(VSpectratypeMean,
                        Arrays.asList(AdditiveCharacteristics.VSpectratypeMean(
                                vSpectratypeMean.preproc(),
                                vSpectratypeMean.weightFunction())),
                        Collections.singletonList(new GroupSummary.Simple<>()))
        );
    }

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class BiophysicsParameters implements WithParentAndTags {
        public MetricParameters cdr3lenAA = new MetricParameters();
        public MetricParameters cdr3lenNT = new MetricParameters();
        public MetricParameters ndnLenNT = new MetricParameters();
        public MetricParameters addedNNT = new MetricParameters();
        public MetricParameters Strength = new MetricParameters();
        public MetricParameters Hydrophobicity = new MetricParameters();
        public MetricParameters Surface = new MetricParameters();
        public MetricParameters Volume = new MetricParameters();
        public MetricParameters Charge = new MetricParameters();

        private List<MetricParameters> list() {
            return Arrays.asList(cdr3lenAA, cdr3lenNT, ndnLenNT, addedNNT, Strength, Hydrophobicity, Surface, Volume, Charge);
        }

        @Override
        public void setParent(PostanalysisParameters parent) {
            for (MetricParameters m : list()) {
                m.setParent(parent);
            }
        }

        @Override
        public void setTagsInfo(TagsInfo tagsInfo) {
            for (MetricParameters m : list()) {
                m.setTagsInfo(tagsInfo);
            }
        }

        public CharacteristicGroup<?, Clone> getGroup() {
            return new CharacteristicGroup<>(Biophysics,
                    Arrays.asList(
                            lengthOf(cdr3lenNT.preproc(), cdr3lenNT.weightFunction(), GeneFeature.CDR3, false).setName("CDR3 length, nt"),
                            lengthOf(cdr3lenAA.preproc(), cdr3lenAA.weightFunction(), GeneFeature.CDR3, true).setName("CDR3 length, aa"),
                            lengthOf(ndnLenNT.preproc(), ndnLenNT.weightFunction(), GeneFeature.VJJunction, false).setName("NDN length, nt"),
                            addedNucleotides(addedNNT.preproc(), addedNNT.weightFunction()).setName("Added N, nt"),
                            biophysics(Strength.preproc(), Strength.weightFunction(), AAProperties.AAProperty.N2Strength, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Strength"),
                            biophysics(Hydrophobicity.preproc(), Hydrophobicity.weightFunction(), AAProperties.AAProperty.N2Hydrophobicity, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Hydrophobicity"),
                            biophysics(Surface.preproc(), Surface.weightFunction(), AAProperties.AAProperty.N2Surface, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Surface"),
                            biophysics(Volume.preproc(), Volume.weightFunction(), AAProperties.AAProperty.N2Volume, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Volume"),
                            biophysics(Charge.preproc(), Charge.weightFunction(), AAProperties.AAProperty.Charge, GeneFeature.CDR3, AAProperties.Adjustment.LeadingCenter, 5).setName("Charge")
                    ),
                    Collections.singletonList(new GroupSummary.Simple<>()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BiophysicsParameters that = (BiophysicsParameters) o;
            return Objects.equals(cdr3lenAA, that.cdr3lenAA) && Objects.equals(cdr3lenNT, that.cdr3lenNT) && Objects.equals(ndnLenNT, that.ndnLenNT) && Objects.equals(addedNNT, that.addedNNT) && Objects.equals(Strength, that.Strength) && Objects.equals(Hydrophobicity, that.Hydrophobicity) && Objects.equals(Surface, that.Surface) && Objects.equals(Volume, that.Volume) && Objects.equals(Charge, that.Charge);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cdr3lenAA, cdr3lenNT, ndnLenNT, addedNNT, Strength, Hydrophobicity, Surface, Volume, Charge);
        }
    }

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class DiversityParameters implements WithParentAndTags {
        public MetricParameters observed = new MetricParameters();
        public MetricParameters shannonWiener = new MetricParameters();
        public MetricParameters chao1 = new MetricParameters();
        public MetricParameters clonality = new MetricParameters();
        public MetricParameters inverseSimpson = new MetricParameters();
        public MetricParameters gini = new MetricParameters();
        public MetricParameters d50 = new MetricParameters();
        public MetricParameters efronThisted = new MetricParameters();

        private List<MetricParameters> list() {
            return Arrays.asList(observed, shannonWiener, chao1, clonality, inverseSimpson, gini, d50, efronThisted);
        }

        @Override
        public void setParent(PostanalysisParameters parent) {
            for (MetricParameters m : list()) {
                m.setParent(parent);
            }
        }

        @Override
        public void setTagsInfo(TagsInfo tagsInfo) {
            for (MetricParameters m : list()) {
                m.setTagsInfo(tagsInfo);
            }
        }

        public CharacteristicGroup<?, Clone> getGroup() {
            List<Characteristic<?, Clone>> chars = new ArrayList<>(groupBy(
                    new HashMap<DiversityMeasure, PreprocessorAndWeight<Clone>>() {{
                        put(DiversityMeasure.Observed, observed.pwTuple());
                        put(DiversityMeasure.ShannonWiener, shannonWiener.pwTuple());
                        put(DiversityMeasure.Chao1, chao1.pwTuple());
                        put(DiversityMeasure.NormalizedShannonWeinerIndex, clonality.pwTuple());
                        put(DiversityMeasure.InverseSimpson, inverseSimpson.pwTuple());
                        put(DiversityMeasure.GiniIndex, gini.pwTuple());
                        put(DiversityMeasure.EfronThisted, efronThisted.pwTuple());
                    }},
                    (p, l) -> Collections.singletonList(new DiversityCharacteristic<>("Diversity "
                            + l.stream().map(m -> m.name).collect(Collectors.joining("/")),
                            p.weight, p.preproc, l.toArray(new DiversityMeasure[0])))));

            chars.add(new DiversityCharacteristic<>("d50", new WeightFunctions.Count(),
                    d50.preproc().then(new SelectTop.Factory<>(WeightFunctions.Count, 0.5)),
                    new DiversityMeasure[]{
                            DiversityMeasure.Observed.overrideName("d50")
                    }));

            //noinspection unchecked,rawtypes
            return new CharacteristicGroup(Diversity,
                    chars,
                    Arrays.asList(new GroupSummary.Simple<>())
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DiversityParameters that = (DiversityParameters) o;
            return Objects.equals(observed, that.observed) && Objects.equals(shannonWiener, that.shannonWiener) && Objects.equals(chao1, that.chao1) && Objects.equals(clonality, that.clonality) && Objects.equals(inverseSimpson, that.inverseSimpson) && Objects.equals(gini, that.gini) && Objects.equals(d50, that.d50);
        }

        @Override
        public int hashCode() {
            return Objects.hash(observed, shannonWiener, chao1, clonality, inverseSimpson, gini, d50);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostanalysisParametersIndividual that = (PostanalysisParametersIndividual) o;
        return Objects.equals(biophysics, that.biophysics) && Objects.equals(diversity, that.diversity) && Objects.equals(vUsage, that.vUsage) && Objects.equals(jUsage, that.jUsage) && Objects.equals(vjUsage, that.vjUsage) && Objects.equals(isotypeUsage, that.isotypeUsage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(biophysics, diversity, vUsage, jUsage, vjUsage, isotypeUsage);
    }
}
