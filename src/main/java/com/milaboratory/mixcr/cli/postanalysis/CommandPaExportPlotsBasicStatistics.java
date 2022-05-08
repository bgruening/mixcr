package com.milaboratory.mixcr.cli.postanalysis;

import com.milaboratory.miplots.stat.util.PValueCorrection;
import com.milaboratory.miplots.stat.util.RefGroup;
import com.milaboratory.miplots.stat.util.TestMethod;
import com.milaboratory.miplots.stat.xcontinious.CorrelationMethod;
import com.milaboratory.mixcr.basictypes.Clone;
import com.milaboratory.mixcr.postanalysis.PostanalysisResult;
import com.milaboratory.mixcr.postanalysis.plots.BasicStatRow;
import com.milaboratory.mixcr.postanalysis.plots.BasicStatistics;
import com.milaboratory.mixcr.postanalysis.ui.CharacteristicGroup;
import org.jetbrains.kotlinx.dataframe.DataFrame;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Objects;

public abstract class CommandPaExportPlotsBasicStatistics extends CommandPaExportPlots {
    abstract String group();

    @Option(description = "Primary group", names = {"-p", "--primary-group"})
    public String primaryGroup;
    @Option(description = "Secondary group", names = {"-s", "--secondary-group"})
    public String secondaryGroup;
    @Option(description = "Facet by", names = {"--facet-by"})
    public String facetBy;
    @Option(description = "Select specific metrics to export.", names = {"--metric"})
    public List<String> metrics;
    @Option(description = "Hide overall p-value.", names = {"--hide-overall-p-value"})
    public boolean hideOverallPValue;
    @Option(description = "Hide pairwise p-values.", names = {"--hide-pairwise-p-value"})
    public boolean hidePairwisePValue;
    @Option(description = "Reference group. Can be \"all\" or some specific value.", names = {"--ref-group"})
    public String refGroup;
    @Option(description = "Hide non-significant observations.", names = {"--hide-ns"})
    public boolean hideNS;
    @Option(description = "Do paired analysis.", names = {"--paired"})
    public boolean paired;
    @Option(description = "Test method. Default is Wilcoxon. Available methods: Wilcoxon, ANOVA, TTest, KruskalWallis, KolmogorovSmirnov", names = {"--method"})
    public String method = "Wilcoxon";
    @Option(description = "Test method for multiple groups comparison. Default is KruskalWallis. Available methods: ANOVA, KruskalWallis, KolmogorovSmirnov", names = {"--method-multiple-groups"})
    public String methodForMultipleGroups = "KruskalWallis";
    @Option(description = "Test method for multiple groups comparison. Default is KruskalWallis. Available methods: none, BenjaminiHochberg, BenjaminiYekutieli, Bonferroni, Hochberg, Holm, Hommel", names = {"--p-adjust-method"})
    public String pAdjustMethod = "Holm";

    @Override
    void run(PaResultByGroup result) {
        CharacteristicGroup<Clone, ?> ch = result.schema.getGroup(group());
        PostanalysisResult paResult = result.result.forGroup(ch);
        DataFrame<?> metadata = metadata();
        DataFrame<BasicStatRow> df = BasicStatistics.INSTANCE.dataFrame(
                paResult,
                metrics,
                metadata
        );

        df = filter(df);

        RefGroup rg = null;
        if (Objects.equals(refGroup, "all"))
            rg = RefGroup.Companion.getAll();
        else if (refGroup != null)
            rg = RefGroup.Companion.of(refGroup);

        BasicStatistics.PlotParameters par = new BasicStatistics.PlotParameters(
                primaryGroup,
                secondaryGroup,
                facetBy,
                !hideOverallPValue,
                !hidePairwisePValue,
                rg,
                hideNS,
                null,
                null,
                null,
                paired,
                TestMethod.valueOf(method),
                TestMethod.valueOf(methodForMultipleGroups),
                pAdjustMethod.equals("none") ? null : PValueCorrection.Method.valueOf(pAdjustMethod),
                CorrelationMethod.Pearson
        );

        List<byte[]> plots = BasicStatistics.INSTANCE.plotsAndSummary(df, par);
        writePlotsAndSummary(result.group, plots);
    }

    @CommandLine.Command(name = "biophysics",
            sortOptions = false,
            separator = " ",
            description = "Export biophysical characteristics")
    public static class ExportBiophysics extends CommandPaExportPlotsBasicStatistics {
        @Override
        String group() {
            return CommandPaIndividual.Biophysics;
        }
    }

    @CommandLine.Command(name = "diversity",
            sortOptions = false,
            separator = " ",
            description = "Export diversity characteristics")
    public static class ExportDiversity extends CommandPaExportPlotsBasicStatistics {
        @Override
        String group() {
            return CommandPaIndividual.Diversity;
        }
    }
}
