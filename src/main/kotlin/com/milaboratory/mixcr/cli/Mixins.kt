/*
 * Copyright (c) 2014-2024, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/mixcr/blob/develop/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
package com.milaboratory.mixcr.cli

import com.milaboratory.app.ValidationException
import com.milaboratory.mitool.tag.TagType
import com.milaboratory.mixcr.cli.CommonDescriptions.Labels
import com.milaboratory.mixcr.cli.MiXCRCommand.OptionsOrder
import com.milaboratory.mixcr.clonegrouping.CellType
import com.milaboratory.mixcr.export.CloneFieldsExtractorsFactory
import com.milaboratory.mixcr.export.CloneGroupFieldsExtractorsFactory
import com.milaboratory.mixcr.export.FieldExtractorsFactory
import com.milaboratory.mixcr.export.VDJCAlignmentsFieldsExtractorsFactory
import com.milaboratory.mixcr.presets.AlignMixins
import com.milaboratory.mixcr.presets.AlignMixins.AlignmentBoundaryConstants
import com.milaboratory.mixcr.presets.AlignMixins.DropNonCDR3Alignments
import com.milaboratory.mixcr.presets.AlignMixins.KeepNonCDR3Alignments
import com.milaboratory.mixcr.presets.AlignMixins.LeftAlignmentBoundaryNoPoint
import com.milaboratory.mixcr.presets.AlignMixins.LeftAlignmentBoundaryWithPoint
import com.milaboratory.mixcr.presets.AlignMixins.LimitInput
import com.milaboratory.mixcr.presets.AlignMixins.MaterialTypeDNA
import com.milaboratory.mixcr.presets.AlignMixins.MaterialTypeRNA
import com.milaboratory.mixcr.presets.AlignMixins.RightAlignmentBoundaryNoPoint
import com.milaboratory.mixcr.presets.AlignMixins.RightAlignmentBoundaryWithPoint
import com.milaboratory.mixcr.presets.AlignMixins.SetLibrary
import com.milaboratory.mixcr.presets.AlignMixins.SetSpecies
import com.milaboratory.mixcr.presets.AlignMixins.SetTagPattern
import com.milaboratory.mixcr.presets.AnalyzeCommandDescriptor.assembleCells
import com.milaboratory.mixcr.presets.AssembleContigsMixins.AssembleContigsWithMaxLength
import com.milaboratory.mixcr.presets.AssembleContigsMixins.SetContigAssemblingFeatures
import com.milaboratory.mixcr.presets.AssembleMixins.SetClonotypeAssemblingFeatures
import com.milaboratory.mixcr.presets.AssembleMixins.SetSplitClonesBy
import com.milaboratory.mixcr.presets.ExportMixins
import com.milaboratory.mixcr.presets.ExportMixins.AddExportAlignmentsField
import com.milaboratory.mixcr.presets.ExportMixins.AddExportCloneGroupsField
import com.milaboratory.mixcr.presets.ExportMixins.AddExportClonesField
import com.milaboratory.mixcr.presets.ExportMixins.DontImputeGermlineOnExport
import com.milaboratory.mixcr.presets.ExportMixins.ImputeGermlineOnExport
import com.milaboratory.mixcr.presets.GenericMixin
import com.milaboratory.mixcr.presets.PipelineMixins.AddPipelineStep
import com.milaboratory.mixcr.presets.PipelineMixins.AssembleContigsByCells
import com.milaboratory.mixcr.presets.PipelineMixins.RemovePipelineStep
import com.milaboratory.mixcr.presets.QcMixins
import com.milaboratory.mixcr.presets.RefineTagsAndSortMixins.DontCorrectTagName
import com.milaboratory.mixcr.presets.RefineTagsAndSortMixins.DontCorrectTagType
import com.milaboratory.mixcr.presets.RefineTagsAndSortMixins.SetWhitelist
import io.repseq.core.GeneFeatures
import io.repseq.core.GeneType
import io.repseq.core.GeneType.Constant
import io.repseq.core.GeneType.Joining
import io.repseq.core.ReferencePoint
import picocli.CommandLine.IParameterConsumer
import picocli.CommandLine.Model.ArgSpec
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import java.util.*

class PipelineMiXCRMixins : MiXCRMixinCollector() {
    //
    // Pipeline manipulation mixins
    //

    @Option(
        description = ["Add a step to pipeline"],
        names = [AddPipelineStep.CMD_OPTION],
        paramLabel = "<step>",
        order = OptionsOrder.mixins.pipeline + 100
    )
    fun addPipelineStep(step: String) =
        mixIn(AddPipelineStep(step))

    @Option(
        description = ["Remove a step from pipeline"],
        names = [RemovePipelineStep.CMD_OPTION],
        paramLabel = "<step>",
        order = OptionsOrder.mixins.pipeline + 200
    )
    fun removePipelineStep(step: String) =
        mixIn(RemovePipelineStep(step))

    // This option PipelineMiXCRMixins because it actually doesn't change assembleClones step, but removing the next.
    // We don't need it assembleClones command
    @Option(
        description = ["Assemble contigs separately by cells. It will cancel `assembleCells` step."],
        names = [AssembleContigsByCells.CMD_OPTION],
        order = OptionsOrder.mixins.assembleContigs + 101
    )
    fun assembleContigsByCells(@Suppress("UNUSED_PARAMETER") f: Boolean) =
        mixIn(AssembleContigsByCells)

    companion object {
        const val DESCRIPTION = "Params to change pipeline steps:%n"
    }
}

// copy of PipelineMiXCRMixins but with hidden fields
class PipelineMiXCRMixinsHidden : MiXCRMixinCollector() {
    //
    // Pipeline manipulation mixins
    //

    @Option(
        hidden = true,
        names = [AddPipelineStep.CMD_OPTION],
    )
    fun addPipelineStep(step: String) =
        mixIn(AddPipelineStep(step))

    @Option(
        hidden = true,
        names = [RemovePipelineStep.CMD_OPTION],
    )
    fun removePipelineStep(step: String) =
        mixIn(RemovePipelineStep(step))

    @Option(
        hidden = true,
        names = [AssembleContigsByCells.CMD_OPTION]
    )
    fun assembleContigsByCells(@Suppress("UNUSED_PARAMETER") f: Boolean) =
        mixIn(AssembleContigsByCells)

}

class RefineTagsAndSortMiXCRMixins : MiXCRMixinCollector() {
    @Option(
        description = [SetWhitelist.DESCRIPTION_SET],
        names = [SetWhitelist.CMD_OPTION_SET],
        paramLabel = Labels.OVERRIDES,
        order = OptionsOrder.mixins.refineTagsAndSort + 100
    )
    fun setWhitelist(assignment: String) = parseAssignment(assignment) { tag, value ->
        mixIn(SetWhitelist(tag, value))
    }

    @Option(
        description = [SetWhitelist.DESCRIPTION_RESET],
        names = [SetWhitelist.CMD_OPTION_RESET],
        paramLabel = Labels.TAG_NAME,
        order = OptionsOrder.mixins.refineTagsAndSort + 200
    )
    fun resetWhitelist(tag: String) {
        mixIn(SetWhitelist(tag, null))
    }

    @Option(
        description = ["Don't correct alignments for tag"],
        names = [DontCorrectTagName.CMD_OPTION],
        paramLabel = Labels.TAG_NAME,
        order = OptionsOrder.mixins.refineTagsAndSort + 300
    )
    fun dontCorrectTag(tag: String) {
        mixIn(DontCorrectTagName(tag))
    }

    @Option(
        description = ["Don't correct alignments for tag type"],
        names = [DontCorrectTagType.CMD_OPTION],
        paramLabel = Labels.TAG_TYPE,
        order = OptionsOrder.mixins.refineTagsAndSort + 301
    )
    fun dontCorrectTagType(tagType: TagType) {
        mixIn(DontCorrectTagType(tagType))
    }

    companion object {
        const val DESCRIPTION = "Params for ${CommandRefineTagsAndSort.COMMAND_NAME} command:%n"
    }
}

class AlignMiXCRMixins : MiXCRMixinCollector() {
    //
    // Base settings
    //

    @Option(
        description = [
            "Species (organism). Possible values: `hsa` (or HomoSapiens), `mmu` (or MusMusculus), `rat`, `spalax`, `alpaca`, `lamaGlama`, `mulatta` (_Macaca Mulatta_), `fascicularis` (_Macaca Fascicularis_) or any species from IMGT ® library."
        ],
        names = [SetSpecies.CMD_OPTION_ALIAS, SetSpecies.CMD_OPTION],
        order = OptionsOrder.mixins.align + 100
    )
    fun species(species: String) =
        mixIn(SetSpecies(species))

    @Option(
        description = ["V/D/J/C gene library. By default, the `default` MiXCR reference library is used. One can also use external libraries"],
        names = [SetLibrary.CMD_OPTION_ALIAS, SetLibrary.CMD_OPTION],
        order = OptionsOrder.mixins.align + 200
    )
    fun library(library: String) =
        mixIn(SetLibrary(library))

    @Option(
        description = ["Split output alignments files by sample."],
        names = [AlignMixins.SetSplitBySample.CMD_OPTION_TRUE],
        arity = "0",
        order = OptionsOrder.mixins.align + 300
    )
    fun splitBySample(@Suppress("UNUSED_PARAMETER") f: Boolean) =
        mixIn(AlignMixins.SetSplitBySample(true))

    @Option(
        description = ["Don't split output alignments files by sample."],
        names = [AlignMixins.SetSplitBySample.CMD_OPTION_FALSE],
        arity = "0",
        order = OptionsOrder.mixins.align + 310
    )
    fun dontSplitBySample(@Suppress("UNUSED_PARAMETER") f: Boolean) =
        mixIn(AlignMixins.SetSplitBySample(false))

    @Option(
        description = ["[Deprecated] Infer sample table (supports only sample tags derived from file names)."],
        names = [AlignMixins.InferSampleTable.CMD_OPTION],
        arity = "0",
        order = OptionsOrder.mixins.align + 320,
        hidden = true,
    )
    fun inferSampleTable(@Suppress("UNUSED_PARAMETER") f: Boolean) {
        println(AlignMixins.InferSampleTable.CMD_OPTION + " deprecated and has no effect.")
    }

    @Option(
        description = ["Loads sample table from a tab separated file (one substitution will be allowed during matching)"],
        names = [AlignMixins.SetSampleSheet.OLD_CMD_OPTION_FUZZY],
        arity = "1",
        paramLabel = "sample_table.tsv",
        hidden = true
    )
    fun sampleTableFuzzy(arg: String) {
        println(
            "Option ${AlignMixins.SetSampleSheet.OLD_CMD_OPTION_FUZZY}is deprecated. Use ${AlignMixins.SetSampleSheet.CMD_OPTION_FUZZY} instead."
        )
        mixIn(AlignMixins.SetSampleSheet(arg, null, true))
    }

    @Option(
        description = ["Loads sample table from a tab separated file (one substitution will be allowed during matching)"],
        names = [AlignMixins.SetSampleSheet.CMD_OPTION_FUZZY],
        arity = "1",
        paramLabel = "sample_sheet.tsv",
        order = OptionsOrder.mixins.align + 330,
    )
    fun sampleSheetFuzzy(arg: String) =
        mixIn(AlignMixins.SetSampleSheet(arg, null, true))

    @Option(
        description = ["Loads sample table from a tab separated file (strict matching will be used)."],
        names = [AlignMixins.SetSampleSheet.OLD_CMD_OPTION_STRICT],
        arity = "1",
        paramLabel = "sample_table.tsv",
        hidden = true
    )
    fun sampleTableStrict(arg: String) {
        println(
            "Option ${AlignMixins.SetSampleSheet.OLD_CMD_OPTION_STRICT}is deprecated. Use ${AlignMixins.SetSampleSheet.CMD_OPTION_STRICT} instead."
        )
        mixIn(AlignMixins.SetSampleSheet(arg, null, false))
    }

    @Option(
        description = ["Loads sample table from a tab separated file (strict matching will be used)."],
        names = [AlignMixins.SetSampleSheet.CMD_OPTION_STRICT],
        arity = "1",
        paramLabel = "sample_sheet.tsv",
        order = OptionsOrder.mixins.align + 335
    )
    fun sampleSheetStrict(arg: String) =
        mixIn(AlignMixins.SetSampleSheet(arg, null, false))

    //
    // Material type
    //

    @Option(
        description = ["For DNA starting material. Setups V gene feature to align to `VGeneWithP` (full intron) and also instructs MiXCR to skip C gene alignment since it is too far from CDR3 in DNA data."],
        names = [MaterialTypeDNA.CMD_OPTION],
        arity = "0",
        order = OptionsOrder.mixins.align + 400
    )
    fun dna(@Suppress("UNUSED_PARAMETER") f: Boolean) =
        mixIn(MaterialTypeDNA)

    @Option(
        description = ["For RNA starting material; setups `VTranscriptWithP` (full exon) gene feature to align for V gene and `CExon1` for C gene."],
        names = [MaterialTypeRNA.CMD_OPTION],
        arity = "0",
        order = OptionsOrder.mixins.align + 500
    )
    fun rna(@Suppress("UNUSED_PARAMETER") f: Boolean) =
        mixIn(MaterialTypeRNA)

    //
    // Alignment boundaries
    //

    @Option(
        description = ["Configures aligners to use semi-local alignment at reads 5'-end. " +
                "Typically used with V gene single primer / multiplex protocols, or if there are non-trimmed adapter sequences at 5'-end. " +
                "Optional <anchor_point> may be specified to instruct MiXCR where the primer is located and strip V feature to align accordingly, resulting in a more precise alignments."],
        names = [AlignmentBoundaryConstants.LEFT_FLOATING_CMD_OPTION],
        arity = "0..1",
        paramLabel = Labels.ANCHOR_POINT,
        order = OptionsOrder.mixins.align + 600,
        completionCandidates = ReferencePointsCandidates::class
    )
    fun floatingLeftAlignmentBoundary(arg: ReferencePoint?) =
        mixIn(
            if (arg == null)
                LeftAlignmentBoundaryNoPoint(true)
            else
                LeftAlignmentBoundaryWithPoint(true, arg)
        )

    @Option(
        description = ["Configures aligners to use global alignment at reads 5'-end. " +
                "Typically used for 5'RACE with template switch oligo or a like protocols. " +
                "Optional <anchor_point> may be specified to instruct MiXCR how to strip V feature to align."],
        names = [AlignmentBoundaryConstants.LEFT_RIGID_CMD_OPTION],
        arity = "0..1",
        paramLabel = Labels.ANCHOR_POINT,
        order = OptionsOrder.mixins.align + 700
    )
    fun rigidLeftAlignmentBoundary(arg: ReferencePoint?) =
        mixIn(
            if (arg == null)
                LeftAlignmentBoundaryNoPoint(false)
            else
                LeftAlignmentBoundaryWithPoint(false, arg)
        )

    @Option(
        description = ["Configures aligners to use semi-local alignment at reads 3'-end. " +
                "Typically used with J or C gene single primer / multiplex protocols, or if there are non-trimmed adapter sequences at 3'-end. " +
                "Requires either gene type (`J` for J primers / `C` for C primers) or <anchor_point> to be specified. " +
                "In latter case MiXCR will additionally strip feature to align accordingly."],
        names = [AlignmentBoundaryConstants.RIGHT_FLOATING_CMD_OPTION],
        arity = "1",
        paramLabel = "(${Labels.GENE_TYPE}|${Labels.ANCHOR_POINT})",
        order = OptionsOrder.mixins.align + 800,
        completionCandidates = ReferencePointsCandidatesAndGeneType::class
    )
    fun floatingRightAlignmentBoundary(arg: String) =
        mixIn(
            when {
                arg.equals("C", ignoreCase = true) || arg.equals("Constant", ignoreCase = true) ->
                    RightAlignmentBoundaryNoPoint(true, Constant)

                arg.equals("J", ignoreCase = true) || arg.equals("Joining", ignoreCase = true) ->
                    RightAlignmentBoundaryNoPoint(true, Joining)

                else ->
                    RightAlignmentBoundaryWithPoint(true, ReferencePoint.parse(arg))
            }
        )

    @Option(
        description = ["Configures aligners to use global alignment at reads 3'-end. " +
                "Typically used for J-C intron single primer / multiplex protocols. " +
                "Optional <gene_type> (`J` for J primers / `C` for C primers) or <anchor_point> may be specified to instruct MiXCR where how to strip J or C feature to align."],
        names = [AlignmentBoundaryConstants.RIGHT_RIGID_CMD_OPTION],
        arity = "0..1",
        paramLabel = "(${Labels.GENE_TYPE}|${Labels.ANCHOR_POINT})",
        order = OptionsOrder.mixins.align + 900,
        completionCandidates = ReferencePointsCandidatesAndGeneType::class
    )
    fun rigidRightAlignmentBoundary(arg: String?) =
        mixIn(
            when {
                arg.isNullOrBlank() ->
                    RightAlignmentBoundaryNoPoint(false, null)

                arg.equals("C", ignoreCase = true) || arg.equals("Constant", ignoreCase = true) ->
                    RightAlignmentBoundaryNoPoint(false, Constant)

                arg.equals("J", ignoreCase = true) || arg.equals("Joining", ignoreCase = true) ->
                    RightAlignmentBoundaryNoPoint(false, Joining)

                else ->
                    RightAlignmentBoundaryWithPoint(false, ReferencePoint.parse(arg))
            }
        )

    @Option(
        description = ["Specify tag pattern for barcoded data."],
        names = [SetTagPattern.CMD_OPTION],
        paramLabel = "<pattern>",
        order = OptionsOrder.mixins.align + 1_000
    )
    fun tagPattern(pattern: String) =
        mixIn(SetTagPattern(pattern))

    @Option(
        description = ["Preserve alignments that do not cover CDR3 region or cover it only partially in the .vdjca file."],
        names = [KeepNonCDR3Alignments.CMD_OPTION],
        arity = "0",
        order = OptionsOrder.mixins.align + 1_100
    )
    fun keepNonCDR3Alignments(@Suppress("UNUSED_PARAMETER") ignored: Boolean) =
        mixIn(KeepNonCDR3Alignments)

    @Option(
        description = ["Drop all alignments that do not cover CDR3 region or cover it only partially."],
        names = [DropNonCDR3Alignments.CMD_OPTION],
        arity = "0",
        order = OptionsOrder.mixins.align + 1_200
    )
    fun dropNonCDR3Alignments(@Suppress("UNUSED_PARAMETER") ignored: Boolean) =
        mixIn(DropNonCDR3Alignments)

    @Option(
        description = ["Maximal number of reads to process on `align`"],
        names = [LimitInput.CMD_OPTION],
        paramLabel = "<n>",
        order = OptionsOrder.mixins.align + 1_300
    )
    fun limitInput(number: Long) =
        mixIn(LimitInput(number))


    companion object {
        const val DESCRIPTION = "Params for ${CommandAlign.COMMAND_NAME} command:%n"
    }
}

class AssembleMiXCRMixins : MiXCRMixinCollector() {
    @Option(
        description = ["Specify gene features used to assemble clonotypes. " +
                "One may specify any custom gene region (e.g. `FR3+CDR3`); target clonal sequence can even be disjoint. " +
                "Note that `assemblingFeatures` must cover CDR3"],
        names = [SetClonotypeAssemblingFeatures.CMD_OPTION],
        paramLabel = Labels.GENE_FEATURES,
        order = OptionsOrder.mixins.assemble + 100,
        completionCandidates = GeneFeaturesCandidates::class
    )
    fun assembleClonotypesBy(gf: GeneFeatures) =
        mixIn(SetClonotypeAssemblingFeatures(gf))


    @Option(
        description = ["Clones with equal clonal sequence but different gene will not be merged."],
        names = [SetSplitClonesBy.CMD_OPTION_TRUE],
        paramLabel = Labels.GENE_TYPE,
        hideParamSyntax = true,
        order = OptionsOrder.mixins.assemble + 200
    )
    fun splitClonesBy(geneTypes: List<GeneType>) =
        geneTypes.forEach { geneType -> mixIn(SetSplitClonesBy(geneType, true)) }

    @Option(
        description = ["Clones with equal clonal sequence but different gene will be merged into single clone."],
        names = [SetSplitClonesBy.CMD_OPTION_FALSE],
        paramLabel = Labels.GENE_TYPE,
        hideParamSyntax = true,
        order = OptionsOrder.mixins.assemble + 300
    )
    fun dontSplitClonesBy(geneTypes: List<GeneType>) =
        geneTypes.forEach { geneType -> mixIn(SetSplitClonesBy(geneType, false)) }

    companion object {
        const val DESCRIPTION = "Params for ${CommandAssemble.COMMAND_NAME} command:%n"
    }
}

class AssembleContigsMiXCRMixins : MiXCRMixinCollector() {
    @Option(
        description = ["Selects the region of interest for the action. Clones will be separated if inconsistent " +
                "nucleotides will be detected in the region, assembling procedure will be limited to the region, " +
                "and only clonotypes that fully cover the region will be outputted, others will be filtered out."],
        names = [SetContigAssemblingFeatures.CMD_OPTION],
        paramLabel = Labels.GENE_FEATURES,
        order = OptionsOrder.mixins.assembleContigs + 100,
        completionCandidates = GeneFeaturesCandidates::class
    )
    fun assembleContigsBy(gf: GeneFeatures) =
        mixIn(SetContigAssemblingFeatures(gf))

    @Option(
        description = ["Contigs with maximum possible length will be assembled."],
        names = [AssembleContigsWithMaxLength.CMD_OPTION],
        paramLabel = Labels.GENE_FEATURES,
        order = OptionsOrder.mixins.assembleContigs + 102
    )
    fun assembleContigsWithMaxLength(ignored: Boolean) =
        mixIn(AssembleContigsWithMaxLength)

    companion object {
        const val DESCRIPTION = "Params for ${CommandAssembleContigs.COMMAND_NAME} command:%n"
    }
}

object ExportMiXCRMixins {

    class All : Modifiers, Generic, ExportClonesMixins, ExportCloneGroupsMixins, MiXCRMixinCollector()

    class CommandSpecific {
        class ExportAlignments : Modifiers, MiXCRMixinCollector()

        class ExportClones : Modifiers, ExportClonesMixins, GeneralExportClonesMixins, MiXCRMixinCollector()

        class ExportCloneGroups : Modifiers, ExportCloneGroupsMixins, MiXCRMixinCollector()
    }

    private interface Modifiers : MiXCRMixinRegister {
        @Option(
            description = ["Export nucleotide sequences using letters from germline (marked lowercase) for uncovered regions"],
            names = [ImputeGermlineOnExport.CMD_OPTION],
            arity = "0",
            order = OptionsOrder.mixins.exports + 100
        )
        fun imputeGermlineOnExport(ignored: Boolean) =
            mixIn(ImputeGermlineOnExport)

        @Option(
            description = ["Export nucleotide sequences only from covered region"],
            names = [DontImputeGermlineOnExport.CMD_OPTION],
            arity = "0",
            order = OptionsOrder.mixins.exports + 200
        )
        fun dontImputeGermlineOnExport(ignored: Boolean) =
            mixIn(DontImputeGermlineOnExport)
    }

    private interface Generic : MiXCRMixinRegister {
        fun addExportClonesField(args: List<String>, prepend: Boolean) {
            require(args.isNotEmpty())
            mixIn(AddExportClonesField(if (prepend) 0 else -1, args.first(), args.drop(1)))
        }

        fun addExportCloneGroupsField(args: List<String>, prepend: Boolean) {
            require(args.isNotEmpty())
            mixIn(AddExportCloneGroupsField(if (prepend) 0 else -1, args.first(), args.drop(1)))
        }

        fun addExportAlignmentsField(args: List<String>, prepend: Boolean) {
            require(args.isNotEmpty())
            mixIn(AddExportAlignmentsField(if (prepend) 0 else -1, args.first(), args.drop(1)))
        }

        @Option(
            description = ["Add clones export column before other columns. First param is field name as it is in `${CommandExportClones.COMMAND_NAME}` command, left params are params of the field"],
            names = [AddExportClonesField.CMD_OPTION_PREPEND_PREFIX],
            parameterConsumer = CloneExportParameterConsumer::class,
            arity = "1..*",
            paramLabel = "<field> [<param>...]",
            hideParamSyntax = true,
            order = OptionsOrder.mixins.exports + 300
        )
        fun prependExportClonesField(data: List<String>) = addExportClonesField(data, true)

        @Option(
            description = ["Add clones export column after other columns. First param is field name as it is in `${CommandExportClones.COMMAND_NAME}` command, left params are params of the field"],
            names = [AddExportClonesField.CMD_OPTION_APPEND_PREFIX],
            parameterConsumer = CloneExportParameterConsumer::class,
            arity = "1..*",
            paramLabel = "<field> [<param>...]",
            hideParamSyntax = true,
            order = OptionsOrder.mixins.exports + 400
        )
        fun appendExportClonesField(data: List<String>) = addExportClonesField(data, false)

        @Option(
            description = ["Add clone groups export column before other columns. First param is field name as it is in `${CommandExportCloneGroups.COMMAND_NAME}` command, left params are params of the field"],
            names = [AddExportCloneGroupsField.CMD_OPTION_PREPEND_PREFIX],
            parameterConsumer = CloneGroupExportParameterConsumer::class,
            arity = "1..*",
            paramLabel = "<field> [<param>...]",
            hideParamSyntax = true,
            order = OptionsOrder.mixins.exports + 500
        )
        fun prependExportCloneGroupsField(data: List<String>) = addExportCloneGroupsField(data, true)

        @Option(
            description = ["Add clone groups export column after other columns. First param is field name as it is in `${CommandExportCloneGroups.COMMAND_NAME}` command, left params are params of the field"],
            names = [AddExportCloneGroupsField.CMD_OPTION_APPEND_PREFIX],
            parameterConsumer = CloneGroupExportParameterConsumer::class,
            arity = "1..*",
            paramLabel = "<field> [<param>...]",
            hideParamSyntax = true,
            order = OptionsOrder.mixins.exports + 600
        )
        fun appendExportCloneGroupsField(data: List<String>) = addExportCloneGroupsField(data, false)

        @Option(
            description = ["Add clones export column before other columns. First param is field name as it is in `${CommandExportAlignments.COMMAND_NAME}` command, left params are params of the field"],
            names = [AddExportAlignmentsField.CMD_OPTION_PREPEND_PREFIX],
            parameterConsumer = AlignsExportParameterConsumer::class,
            arity = "1..*",
            paramLabel = "<field> [<param>...]",
            hideParamSyntax = true,
            order = OptionsOrder.mixins.exports + 700
        )
        fun prependExportAlignmentsField(data: List<String>) = addExportAlignmentsField(data, true)

        @Option(
            description = ["Add clones export column after other columns. First param is field name as it is in `${CommandExportAlignments.COMMAND_NAME}` command, left params are params of the field"],
            names = [AddExportAlignmentsField.CMD_OPTION_APPEND_PREFIX],
            parameterConsumer = AlignsExportParameterConsumer::class,
            arity = "1..*",
            paramLabel = "<field> [<param>...]",
            hideParamSyntax = true,
            order = OptionsOrder.mixins.exports + 800
        )
        fun appendExportAlignmentsField(data: List<String>) = addExportAlignmentsField(data, false)

        companion object {
            abstract class ExportParameterConsumer(private val fieldsFactory: FieldExtractorsFactory<*>) :
                IParameterConsumer {
                override fun consumeParameters(args: Stack<String>, argSpec: ArgSpec, commandSpec: CommandSpec) {
                    val fieldName = args.pop()
                    val field = fieldsFactory[fieldName]
                    val argsCountToAdd = field.consumableArgs(args.reversed())
                    if (argsCountToAdd > args.size) {
                        throw ValidationException("Not enough parameters for ${field.cmdArgName}")
                    }
                    val actualArgs: MutableList<String> = mutableListOf(fieldName)
                    repeat(argsCountToAdd) {
                        actualArgs.add(args.pop())
                    }
                    argSpec.setValue(actualArgs)
                }
            }

            class CloneExportParameterConsumer : ExportParameterConsumer(CloneFieldsExtractorsFactory)

            class CloneGroupExportParameterConsumer :
                ExportParameterConsumer(CloneGroupFieldsExtractorsFactory.forAllChains)

            class AlignsExportParameterConsumer : ExportParameterConsumer(VDJCAlignmentsFieldsExtractorsFactory)
        }
    }


    private interface GeneralExportClonesMixins : MiXCRMixinRegister {
        @Option(
            description = ["Export only productive clonotypes."],
            names = [ExportMixins.ExportProductiveClonesOnly.CMD_OPTION],
            arity = "0",
            order = OptionsOrder.mixins.exports + 5_000
        )
        fun exportProductiveClonesOnly(ignored: Boolean) =
            mixIn(ExportMixins.ExportProductiveClonesOnly)

        @Option(
            description = ["Reset all file splitting for output clone and/or clone group tables."],
            names = [ExportMixins.ExportClonesResetFileSplitting.CMD_OPTION],
            arity = "0",
            order = OptionsOrder.mixins.exports + 1_000
        )
        fun resetExportClonesFileSplitting(ignored: Boolean) =
            mixIn(ExportMixins.ExportClonesResetFileSplitting)
    }

    private interface ExportCloneGroupsMixins : GeneralExportClonesMixins {
        @Option(
            description = [
                "Export clone groups for given cell type.",
                "If selected only one cell type, export will be written in one file without prefixes. If several, there will be a file for each type.",
                "All cell groups that don't much specified groups will be filtered out.",
                "Possible values: \${COMPLETION-CANDIDATES}."
            ],
            names = [ExportMixins.ExportCloneGroupsForCellTypes.CMD_OPTION],
            order = OptionsOrder.mixins.exports + 5_100,
            hideParamSyntax = true,
            paramLabel = "<cell_type>",
            arity = "1..*"
        )
        fun exportCloneGroupsForCellTypes(types: List<CellType>) =
            mixIn(ExportMixins.ExportCloneGroupsForCellTypes(types))

        @Option(
            description = ["Export clone groups for all cell types."],
            names = [ExportMixins.ExportCloneGroupsForAllCellTypes.CMD_OPTION],
            order = OptionsOrder.mixins.exports + 5_101,
            arity = "0",
        )
        fun exportCloneGroupsForAllCellTypes(ignored: Boolean) =
            mixIn(ExportMixins.ExportCloneGroupsForAllCellTypes)

        @Option(
            description = ["Show columns for secondary chains in export for cell groups."],
            names = [ExportMixins.ShowSecondaryChainOnExportCellGroups.CMD_OPTION_FOR_SHOW],
            order = OptionsOrder.mixins.exports + 5_200,
            arity = "0",
        )
        fun showSecondaryChainOnExportCellGroups(ignored: Boolean) =
            mixIn(ExportMixins.ShowSecondaryChainOnExportCellGroups(true))

        @Option(
            description = ["Don't show columns for secondary chains in export for cell groups."],
            names = [ExportMixins.ShowSecondaryChainOnExportCellGroups.CMD_OPTION_FOR_DONT_SHOW],
            order = OptionsOrder.mixins.exports + 5_201,
            arity = "0",
        )
        fun dontShowSecondaryChainOnExportCellGroups(ignored: Boolean) =
            mixIn(ExportMixins.ShowSecondaryChainOnExportCellGroups(false))

        @Option(
            description = [
                "How to sort clones for determination of the primary and the secondary chains.",
                "Read - by reads count, Molecule - by count of UMI tags, Auto - by UMI if it's available, by Read otherwise"
            ],
            names = [ExportMixins.ExportCloneGroupsSortChainsBy.CMD_OPTION],
            order = OptionsOrder.mixins.exports + 5_300,
            paramLabel = "(Read|Molecule|Auto)",
            arity = "1"
        )
        fun sortChainsBy(type: CommandExportCloneGroupsParams.SortChainsBy) =
            mixIn(ExportMixins.ExportCloneGroupsSortChainsBy(type))
    }

    private interface ExportClonesMixins : GeneralExportClonesMixins {
        @Option(
            description = ["Add key to split output files with clone tables."],
            names = [ExportMixins.ExportClonesAddFileSplitting.CMD_OPTION],
            paramLabel = "<(geneLabel|tag):key>",
            arity = "1",
            order = OptionsOrder.mixins.exports + 900
        )
        fun addExportClonesFileSplitting(by: String) =
            mixIn(ExportMixins.ExportClonesAddFileSplitting(by))

        @Option(
            description = ["Add key to group clones in the output clone tables."],
            names = [ExportMixins.ExportClonesAddCloneGrouping.CMD_OPTION],
            arity = "1",
            paramLabel = "<(geneLabel|tag):key>",
            order = OptionsOrder.mixins.exports + 1_100
        )
        fun addExportClonesCloneGrouping(by: String) =
            mixIn(ExportMixins.ExportClonesAddCloneGrouping(by))

        @Option(
            description = ["Reset all clone grouping in the output clone tables."],
            names = [ExportMixins.ExportClonesResetCloneGrouping.CMD_OPTION],
            arity = "0",
            order = OptionsOrder.mixins.exports + 1_200
        )
        fun resetExportClonesCloneGrouping(ignored: Boolean) =
            mixIn(ExportMixins.ExportClonesResetCloneGrouping)

        @Option(
            description = [
                "Filter out clones from groups of particular type.",
                "`found` - groups that were found on `${assembleCells.name}`.",
                "`undefined` - there were not enough info on `${assembleCells.name}` to form a group.",
                "`contamination` - clones that were marked as contamination on `${assembleCells.name}`.",
            ],
            names = [ExportMixins.FilterOutCloneGroupTypes.CMD_OPTION],
            arity = "1..*",
            hideParamSyntax = true,
            paramLabel = "(found|undefined|contamination)",
            order = OptionsOrder.mixins.exports + 1_300
        )
        fun filterOutCloneGroupTypes(types: List<CommandExportClonesParams.CloneGroupTypes>) =
            mixIn(ExportMixins.FilterOutCloneGroupTypes(types))

        @Option(
            description = ["Reset filter of clones by group type."],
            names = [ExportMixins.ResetFilterOfCloneGroupTypes.CMD_OPTION],
            arity = "0",
            order = OptionsOrder.mixins.exports + 1_400
        )
        fun resetFilterOfCloneGroupTypes(ignored: Boolean) =
            mixIn(ExportMixins.ResetFilterOfCloneGroupTypes)
    }

    const val DESCRIPTION = "Params for export commands:%n"
}

class QcChecksMixins : MiXCRMixinCollector() {
    @Option(
        description = ["Remove qc check with given type. Use `exportPreset` command to see what `qc.checks` are included in the preset."],
        names = [QcMixins.RemoveQcChecks.CMD_OPTION],
        arity = "1..*",
        paramLabel = "<type>",
        hideParamSyntax = true,
        order = OptionsOrder.mixins.qc + 100,
    )
    fun removeQc(types: Set<String>) =
        mixIn(QcMixins.RemoveQcChecks(types))
}

class GenericMiXCRMixins : MiXCRMixinCollector() {
    @Option(
        description = [GenericMixin.DESCRIPTION],
        names = [GenericMixin.CMD_OPTION],
        paramLabel = Labels.OVERRIDES,
        order = OptionsOrder.overrides + 300
    )
    fun genericMixin(assignment: String) = parseAssignment(assignment) { field, value ->
        mixIn(GenericMixin(field, value))
    }
}

private fun parseAssignment(assignment: String, action: (field: String, value: String) -> Unit) {
    val equalitySignPosition = assignment.indexOf('=')
    ValidationException.require(equalitySignPosition > 0) {
        "Can't parse $assignment, it should contain '=' symbol"
    }
    val field = assignment.substring(0, equalitySignPosition)
    val value = assignment.substring(equalitySignPosition + 1)
    action(field, value)
}
