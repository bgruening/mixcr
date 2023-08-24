/*
 * Copyright (c) 2014-2023, MiLaboratories Inc. All Rights Reserved
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

import com.milaboratory.app.InputFileType
import com.milaboratory.app.ValidationException
import com.milaboratory.app.logger
import com.milaboratory.cli.POverridesBuilderOps
import com.milaboratory.mixcr.basictypes.ClnsReader
import com.milaboratory.mixcr.basictypes.ClnsWriter
import com.milaboratory.mixcr.basictypes.Clone
import com.milaboratory.mixcr.basictypes.CloneSet
import com.milaboratory.mixcr.clonegrouping.CloneGroupingParams.Companion.mkGrouper
import com.milaboratory.mixcr.presets.MiXCRCommandDescriptor
import com.milaboratory.mixcr.presets.MiXCRParamsBundle
import com.milaboratory.util.ReportUtil
import com.milaboratory.util.SmartProgressReporter
import io.repseq.core.VDJCLibraryRegistry
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters
import java.nio.file.Path

object CommandGroupClones {
    const val COMMAND_NAME = MiXCRCommandDescriptor.groupClones.name

    abstract class CmdBase : MiXCRCommandWithOutputs(), MiXCRPresetAwareCommand<CommandGroupClonesParams> {
        override val paramsResolver =
            object : MiXCRParamsResolver<CommandGroupClonesParams>(MiXCRParamsBundle::groupClones) {
                override fun POverridesBuilderOps<CommandGroupClonesParams>.paramsOverrides() {}
            }
    }

    @Command(
        description = ["Impute alignments or clones with germline sequences."]
    )
    class Cmd : CmdBase() {
        @Parameters(
            description = ["Path to input file."],
            paramLabel = "clones.clns",
            index = "0"
        )
        lateinit var inputFile: Path

        @Parameters(
            description = ["Path where to write output. Will have the same file type."],
            paramLabel = "grouped.clns",
            index = "1"
        )
        lateinit var outputFile: Path

        @Mixin
        lateinit var reportOptions: ReportOptions

        @Mixin
        lateinit var resetPreset: ResetPresetOptions

        @Mixin
        lateinit var dontSavePresetOption: DontSavePresetOption

        override val inputFiles
            get() = listOf(inputFile)

        override val outputFiles
            get() = listOf(outputFile)

        override fun validate() {
            ValidationException.requireTheSameFileType(
                inputFile,
                outputFile,
                InputFileType.CLNS
            )
        }

        override fun run1() {
            ClnsReader(inputFile, VDJCLibraryRegistry.getDefault()).use { reader ->
                val input = reader.readCloneSet()
                ValidationException.require(input.none { it.group != null }) {
                    "Input file already grouped by cells"
                }

                val paramsSpec = resetPreset.overridePreset(reader.header.paramsSpec)
                val (_, cmdParams) = paramsResolver.resolve(paramsSpec, printParameters = logger.verbose)


                val reportBuilder = CloneGroupingReport.Builder()
                reportBuilder.setStartMillis(System.currentTimeMillis())
                reportBuilder.setInputFiles(inputFiles)
                reportBuilder.setOutputFiles(outputFiles)
                reportBuilder.commandLine = commandLineArguments

                val grouper = cmdParams.parameters.mkGrouper<Clone>(input.tagsInfo)
                SmartProgressReporter.startProgressReport(grouper)
                val grouppedClones = grouper.groupClones(input.clones)
                reportBuilder.grouperReport = grouper.getReport()

                val result = CloneSet.Builder(
                    grouppedClones,
                    input.usedGenes,
                    input.header.addStepParams(MiXCRCommandDescriptor.groupClones, cmdParams)
                )
                    .sort(input.ordering)
                    // some clones split, need to recalculate
                    .recalculateRanks()
                    // total sums are not changed
                    .withTotalCounts(input.counts)
                    .build()

                val report: CloneGroupingReport
                ClnsWriter(outputFile).use { writer ->
                    writer.writeCloneSet(result)
                    reportBuilder.setFinishMillis(System.currentTimeMillis())
                    report = reportBuilder.buildReport()
                    writer.setFooter(reader.footer.addStepReport(MiXCRCommandDescriptor.groupClones, report))
                }

                ReportUtil.writeReportToStdout(report)
                reportOptions.appendToFiles(report)
            }
        }
    }
}
