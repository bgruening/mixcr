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
package com.milaboratory.mixcr.cli

import com.milaboratory.mixcr.AlignMixins.AlignmentBoundaryConstants
import com.milaboratory.mixcr.AlignMixins.LeftAlignmentBoundaryNoPoint
import com.milaboratory.mixcr.AlignMixins.LeftAlignmentBoundaryWithPoint
import com.milaboratory.mixcr.AlignMixins.LimitInput
import com.milaboratory.mixcr.AlignMixins.MaterialTypeDNA
import com.milaboratory.mixcr.AlignMixins.MaterialTypeRNA
import com.milaboratory.mixcr.AlignMixins.RightAlignmentBoundaryNoPoint
import com.milaboratory.mixcr.AlignMixins.RightAlignmentBoundaryWithPoint
import com.milaboratory.mixcr.AlignMixins.SetLibrary
import com.milaboratory.mixcr.AlignMixins.SetSpecies
import com.milaboratory.mixcr.AlignMixins.SetTagPattern
import com.milaboratory.mixcr.AssembleContigsMixins.SetContigAssemblingFeatures
import com.milaboratory.mixcr.AssembleMixins.DropNonCDR3Alignments
import com.milaboratory.mixcr.AssembleMixins.KeepNonCDR3Alignments
import com.milaboratory.mixcr.AssembleMixins.SetClonotypeAssemblingFeatures
import com.milaboratory.mixcr.AssembleMixins.SetSplitClonesBy
import com.milaboratory.mixcr.ExportMixins.AddExportAlignmentsField
import com.milaboratory.mixcr.ExportMixins.AddExportClonesField
import com.milaboratory.mixcr.ExportMixins.DontImputeGermlineOnExport
import com.milaboratory.mixcr.ExportMixins.ImputeGermlineOnExport
import com.milaboratory.mixcr.GenericMixin
import com.milaboratory.mixcr.PipelineMixins.AddPipelineStep
import com.milaboratory.mixcr.PipelineMixins.RemovePipelineStep
import com.milaboratory.mixcr.basictypes.GeneFeatures
import com.milaboratory.mixcr.export.CloneFieldsExtractorsFactory
import com.milaboratory.mixcr.export.FieldExtractorsFactoryNew
import com.milaboratory.mixcr.export.VDJCAlignmentsFieldsExtractorsFactory
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
        paramLabel = "<step>"
    )
    fun addPipelineStep(step: String) =
        mixIn(AddPipelineStep(step))

    @Option(
        description = ["Remove a step from pipeline"],
        names = [RemovePipelineStep.CMD_OPTION],
        paramLabel = "<step>"
    )
    fun removePipelineStep(step: String) =
        mixIn(RemovePipelineStep(step))

    companion object {
        const val DESCRIPTION = "Params to change pipeline steps\n"
    }
}

//copy of PipelineMiXCRMixins but with hidden fields
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
}

class AlignMiXCRMixins : MiXCRMixinCollector() {
    //
    // Base settings
    //

    @Option(
        description = [CommonDescriptions.SPECIES],
        names = [SetSpecies.CMD_OPTION_ALIAS, SetSpecies.CMD_OPTION]
    )
    fun species(species: String) =
        mixIn(SetSpecies(species))

    @Option(
        description = ["V/D/J/C gene library"],
        names = [SetLibrary.CMD_OPTION_ALIAS, SetLibrary.CMD_OPTION]
    )
    fun library(library: String) =
        mixIn(SetLibrary(library))

    @Option(
        description = ["Maximal number of reads to process on 'align'"],
        names = [LimitInput.CMD_OPTION],
        paramLabel = "<n>"
    )
    fun limitInput(number: Long) =
        mixIn(LimitInput(number))

    //
    // Material type
    //

    @Option(
        description = ["Mark that sample material is DNA"],
        names = [MaterialTypeDNA.CMD_OPTION],
        arity = "0"
    )
    fun dna(f: Boolean) =
        mixIn(MaterialTypeDNA)

    @Option(
        description = ["Mark that sample material is RNA"],
        names = [MaterialTypeRNA.CMD_OPTION],
        arity = "0"
    )
    fun rna(f: Boolean) =
        mixIn(MaterialTypeRNA)

    //
    // Alignment boundaries
    //

    @Option(
        description = [],
        names = [AlignmentBoundaryConstants.LEFT_FLOATING_CMD_OPTION],
        arity = "0..1",
        paramLabel = "<anchor_point>"
    )
    fun floatingLeftAlignmentBoundary(arg: String?) =
        mixIn(
            if (arg.isNullOrBlank())
                LeftAlignmentBoundaryNoPoint(true)
            else
                LeftAlignmentBoundaryWithPoint(true, ReferencePoint.parse(arg))
        )

    @Option(
        description = [],
        names = [AlignmentBoundaryConstants.LEFT_RIGID_CMD_OPTION],
        arity = "0..1",
        paramLabel = "<anchor_point>"
    )
    fun rigidLeftAlignmentBoundary(arg: String?) =
        mixIn(
            if (arg.isNullOrBlank())
                LeftAlignmentBoundaryNoPoint(false)
            else
                LeftAlignmentBoundaryWithPoint(false, ReferencePoint.parse(arg))
        )

    @Option(
        description = [],
        names = [AlignmentBoundaryConstants.RIGHT_FLOATING_CMD_OPTION],
        arity = "1",
        paramLabel = "<anchor_point>"
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
        description = [],
        names = [AlignmentBoundaryConstants.RIGHT_RIGID_CMD_OPTION],
        arity = "0..1",
        paramLabel = "<anchor_point>"
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
        description = ["Tag pattern to extract from the read."],
        names = [SetTagPattern.CMD_OPTION],
        paramLabel = "<pattern>"
    )
    fun tagPattern(pattern: String) =
        mixIn(SetTagPattern(pattern))

    companion object {
        const val DESCRIPTION = "Params for ${CommandAlign.COMMAND_NAME} command\n"
    }
}

class AssembleMiXCRMixins : MiXCRMixinCollector() {
    @Option(
        description = ["Specify gene features used to assemble clonotypes. One may specify any custom gene region (e.g. `FR3+CDR3`); target clonal sequence can even be disjoint. Note that `assemblingFeatures` must cover CDR3"],
        names = [SetClonotypeAssemblingFeatures.CMD_OPTION],
        paramLabel = "<gene_features>"
    )
    fun assembleClonotypesBy(gf: String) =
        mixIn(SetClonotypeAssemblingFeatures(GeneFeatures.parse(gf)))

    @Option(
        description = [],
        names = [KeepNonCDR3Alignments.CMD_OPTION],
        arity = "0"
    )
    fun keepNonCDR3Alignments(ignored: Boolean) =
        mixIn(KeepNonCDR3Alignments)

    @Option(
        description = [],
        names = [DropNonCDR3Alignments.CMD_OPTION],
        arity = "0"
    )
    fun dropNonCDR3Alignments(ignored: Boolean) =
        mixIn(DropNonCDR3Alignments)

    @Option(
        description = ["Clones with equal clonal sequence but different gene will not be merged."],
        names = [SetSplitClonesBy.CMD_OPTION_TRUE],
        paramLabel = "<gene_types>.."
    )
    fun splitClonesBy(geneTypes: List<String>) =
        geneTypes.forEach { geneType -> mixIn(SetSplitClonesBy(GeneType.parse(geneType), true)) }

    @Option(
        description = ["Clones with equal clonal sequence but different gene will be merged into single clone."],
        names = [SetSplitClonesBy.CMD_OPTION_FALSE],
        paramLabel = "<gene_types>.."
    )
    fun dontSplitClonesBy(geneTypes: List<String>) =
        geneTypes.forEach { geneType -> mixIn(SetSplitClonesBy(GeneType.parse(geneType), false)) }

    companion object {
        const val DESCRIPTION = "Params for ${CommandAssemble.COMMAND_NAME} command\n"
    }
}

class AssembleContigsMiXCRMixins : MiXCRMixinCollector() {
    @Option(
        description = ["Selects the region of interest for the action. Clones will be separated if inconsistent " +
                "nucleotides will be detected in the region, assembling procedure will be limited to the region, " +
                "and only clonotypes that fully cover the region will be outputted, others will be filtered out."],
        names = [SetContigAssemblingFeatures.CMD_OPTION],
        paramLabel = "<gene_features>"
    )
    fun assembleContigsBy(gf: String) =
        mixIn(SetContigAssemblingFeatures(GeneFeatures.parse(gf)))

    companion object {
        const val DESCRIPTION = "Params for ${CommandAssembleContigs.COMMAND_NAME} command\n"
    }
}

class ExportMiXCRMixins : MiXCRMixinCollector() {
    @Option(
        description = ["Export nucleotide sequences using letters from germline (marked lowercase) for uncovered regions"],
        names = [ImputeGermlineOnExport.CMD_OPTION],
        arity = "0"
    )
    fun imputeGermlineOnExport(ignored: Boolean) =
        mixIn(ImputeGermlineOnExport)

    @Option(
        description = ["Export nucleotide sequences only from covered region"],
        names = [DontImputeGermlineOnExport.CMD_OPTION],
        arity = "0"
    )
    fun dontImputeGermlineOnExport(ignored: Boolean) =
        mixIn(DontImputeGermlineOnExport)

    private fun addExportClonesField(args: List<String>, prepend: Boolean) {
        require(args.isNotEmpty())
        mixIn(AddExportClonesField(if (prepend) 0 else -1, args.first(), args.drop(1)))
    }

    private fun addExportAlignmentsField(args: List<String>, prepend: Boolean) {
        require(args.isNotEmpty())
        mixIn(AddExportAlignmentsField(if (prepend) 0 else -1, args.first(), args.drop(1)))
    }

    @Option(
        description = ["Add clones export column before other columns. First param is field name as it is in '${CommandExportClones.COMMAND_NAME}' command, left params are params of the field"],
        names = [AddExportClonesField.CMD_OPTION_PREPEND_PREFIX],
        parameterConsumer = CloneExportParameterConsumer::class,
        arity = "1..*",
        paramLabel = "<field [params ...]>"
    )
    fun prependExportClonesField(data: List<String>) = addExportClonesField(data, true)

    @Option(
        description = ["Add clones export column after other columns. First param is field name as it is in '${CommandExportClones.COMMAND_NAME}' command, left params are params of the field"],
        names = [AddExportClonesField.CMD_OPTION_APPEND_PREFIX],
        parameterConsumer = CloneExportParameterConsumer::class,
        arity = "1..*",
    )
    fun appendExportClonesField(data: List<String>) = addExportClonesField(data, false)

    @Option(
        description = ["Add clones export column before other columns. First param is field name as it is in '${CommandExportAlignments.COMMAND_NAME}' command, left params are params of the field"],
        names = [AddExportAlignmentsField.CMD_OPTION_PREPEND_PREFIX],
        parameterConsumer = AlignsExportParameterConsumer::class,
        arity = "1..*",
    )
    fun prependExportAlignmentsField(data: List<String>) = addExportAlignmentsField(data, true)

    @Option(
        description = ["Add clones export column after other columns. First param is field name as it is in '${CommandExportAlignments.COMMAND_NAME}' command, left params are params of the field"],
        names = [AddExportAlignmentsField.CMD_OPTION_APPEND_PREFIX],
        parameterConsumer = AlignsExportParameterConsumer::class,
        arity = "1..*",
    )
    fun appendExportAlignmentsField(data: List<String>) = addExportAlignmentsField(data, false)

    companion object {
        const val DESCRIPTION = "Params for export commands\n"

        abstract class ExportParameterConsumer(private val fieldsFactory: FieldExtractorsFactoryNew<*>) :
            IParameterConsumer {
            override fun consumeParameters(args: Stack<String>, argSpec: ArgSpec, commandSpec: CommandSpec) {
                val fieldName = args.pop()
                val argsCount = fieldsFactory.getNArgsForField(fieldName)
                val actualArgs: MutableList<String> = mutableListOf()
                repeat(argsCount) {
                    actualArgs.add(args.pop())
                }
                argSpec.setValue(listOf(fieldName) + actualArgs)
            }
        }

        class CloneExportParameterConsumer : ExportParameterConsumer(CloneFieldsExtractorsFactory)

        class AlignsExportParameterConsumer : ExportParameterConsumer(VDJCAlignmentsFieldsExtractorsFactory)
    }
}

class GenericMiXCRMixins : MiXCRMixinCollector() {
    @Option(
        description = ["Overrides preset parameters"],
        names = [GenericMixin.CMD_OPTION]
    )
    fun genericMixin(fieldAndOverrides: Map<String, String>) {
        fieldAndOverrides.forEach { (field, override) ->
            mixIn(GenericMixin(field, override))
        }
    }
}
