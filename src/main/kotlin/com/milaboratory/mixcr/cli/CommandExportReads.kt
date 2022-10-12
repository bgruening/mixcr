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

import com.milaboratory.core.io.sequence.PairedRead
import com.milaboratory.core.io.sequence.SequenceWriter
import com.milaboratory.core.io.sequence.SingleRead
import com.milaboratory.core.io.sequence.fastq.PairedFastqWriter
import com.milaboratory.core.io.sequence.fastq.SingleFastqWriter
import com.milaboratory.mixcr.basictypes.VDJCAlignmentsReader
import com.milaboratory.primitivio.forEach
import com.milaboratory.util.SmartProgressReporter
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.nio.file.Path

@Command(
    description = ["Export original reads from vdjca file."]
)
class CommandExportReads : MiXCRCommandWithOutputs() {
    @Parameters(description = ["input.vdjca [output_R1.fastq[.gz] [output_R2.fastq[.gz]]]"], arity = "1..3")
    var inOut: List<Path> = mutableListOf()

    override val inputFiles
        get() = listOf(inOut.first())

    public override val outputFiles
        get() = inOut.subList(1, inOut.size)

    override fun run0() {
        VDJCAlignmentsReader(inputFiles.first()).use { reader ->
            createWriter().use { writer ->
                SmartProgressReporter.startProgressReport("Extracting reads", reader, System.err)
                reader.forEach { alignments ->
                    val reads = alignments.originalReads
                        ?: throw ApplicationException("VDJCA file doesn't contain original reads (perform align action with -g / --save-reads option).")
                    for (read in reads) {
                        when (writer) {
                            is PairedFastqWriter -> {
                                if (read.numberOfReads() == 1)
                                    throw ValidationException("VDJCA file contains single-end reads, but two output files are specified.")
                                writer.write(read as PairedRead)
                            }
                            is SingleFastqWriter -> {
                                if (read.numberOfReads() == 2)
                                    throw ValidationException("VDJCA file contains paired-end reads, but only one / no output file is specified.")
                                writer.write(read as SingleRead)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createWriter(): SequenceWriter<*> {
        val outputFiles = outputFiles
        return when (outputFiles.size) {
            0 -> SingleFastqWriter(System.out)
            1 -> SingleFastqWriter(outputFiles[0].toFile())
            2 -> PairedFastqWriter(outputFiles[0].toFile(), outputFiles[1].toFile())
            else -> throw IllegalArgumentException()
        }
    }
}
