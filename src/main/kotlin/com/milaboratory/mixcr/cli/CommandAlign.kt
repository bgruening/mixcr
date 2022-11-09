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


import cc.redberry.pipe.OutputPort
import cc.redberry.pipe.OutputPortCloseable
import cc.redberry.pipe.util.CountLimitingOutputPort
import cc.redberry.pipe.util.StatusReporter
import cc.redberry.pipe.util.StatusReporter.StatusProvider
import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.annotation.JsonProperty
import com.milaboratory.cli.POverridesBuilderOps
import com.milaboratory.core.io.CompressionType
import com.milaboratory.core.io.sequence.*
import com.milaboratory.core.io.sequence.fasta.FastaReader
import com.milaboratory.core.io.sequence.fasta.FastaSequenceReaderWrapper
import com.milaboratory.core.io.sequence.fastq.PairedFastqReader
import com.milaboratory.core.io.sequence.fastq.PairedFastqWriter
import com.milaboratory.core.io.sequence.fastq.SingleFastqReader
import com.milaboratory.core.io.sequence.fastq.SingleFastqWriter
import com.milaboratory.core.sequence.NSQTuple
import com.milaboratory.core.sequence.NucleotideSequence
import com.milaboratory.core.sequence.quality.QualityTrimmerParameters
import com.milaboratory.core.sequence.quality.ReadTrimmerProcessor
import com.milaboratory.milm.MiXCRMain
import com.milaboratory.mitool.helpers.*
import com.milaboratory.mitool.helpers.map
import com.milaboratory.mitool.pattern.search.*
import com.milaboratory.mitool.pattern.search.ReadSearchPlan.Companion.create
import com.milaboratory.mitool.report.ParseReportAggregator
import com.milaboratory.mitool.use
import com.milaboratory.mixcr.*
import com.milaboratory.mixcr.AlignMixins.LimitInput
import com.milaboratory.mixcr.bam.BAMReader
import com.milaboratory.mixcr.basictypes.*
import com.milaboratory.mixcr.basictypes.tag.*
import com.milaboratory.mixcr.cli.CommandAlign.Cmd.InputType.*
import com.milaboratory.mixcr.cli.CommandAlign.Cmd.ProcessingBundleStatus.*
import com.milaboratory.mixcr.cli.CommonDescriptions.DEFAULT_VALUE_FROM_PRESET
import com.milaboratory.mixcr.cli.CommonDescriptions.Labels
import com.milaboratory.mixcr.util.toHexString
import com.milaboratory.mixcr.vdjaligners.VDJCAligner
import com.milaboratory.mixcr.vdjaligners.VDJCAlignerParameters
import com.milaboratory.mixcr.vdjaligners.VDJCAlignmentFailCause
import com.milaboratory.primitivio.*
import com.milaboratory.util.*
import io.repseq.core.Chains
import io.repseq.core.GeneFeature.*
import io.repseq.core.GeneType
import io.repseq.core.VDJCLibrary
import io.repseq.core.VDJCLibraryRegistry
import jetbrains.datalore.plot.config.asMutable
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Model.PositionalParamSpec
import java.io.FileInputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.math.max

object CommandAlign {
    const val COMMAND_NAME = "align"

    data class Params(
        @JsonProperty("species") val species: String = "",
        @JsonProperty("libraryName") val library: String = "default",
        @JsonProperty("trimmingQualityThreshold") val trimmingQualityThreshold: Byte,
        @JsonProperty("trimmingWindowSize") val trimmingWindowSize: Byte,
        @JsonProperty("chains") val chains: String = "ALL",
        @JsonProperty("overlapPairedReads") val overlapPairedReads: Boolean = true,
        @JsonProperty("bamDropNonVDJ") val bamDropNonVDJ: Boolean = false,
        @JsonProperty("writeFailedAlignments") val writeFailedAlignments: Boolean = false,
        @JsonProperty("tagPattern") val tagPattern: String? = null,
        @JsonProperty("tagUnstranded") val tagUnstranded: Boolean = false,
        @JsonProperty("tagMaxBudget") val tagMaxBudget: Double,
        @JsonProperty("readIdAsCellTag") val readIdAsCellTag: Boolean = false,
        @JsonProperty("limit") val limit: Long? = null,
        @JsonProperty("parameters") @JsonMerge val parameters: VDJCAlignerParameters,
    ) : MiXCRParams {
        override val command get() = MiXCRCommandDescriptor.align
    }

    class PathsForNotAligned {
        @set:Option(
            description = ["Pipe not aligned R1 reads into separate file."],
            names = ["--not-aligned-R1"],
            paramLabel = "<path.fastq[.gz]>"
        )
        var notAlignedReadsR1: Path? = null
            set(value) {
                ValidationException.requireFileType(value, InputFileType.FASTQ)
                field = value
            }

        @set:Option(
            description = ["Pipe not aligned R2 reads into separate file."],
            names = ["--not-aligned-R2"],
            paramLabel = "<path.fastq[.gz]>"
        )
        var notAlignedReadsR2: Path? = null
            set(value) {
                ValidationException.requireFileType(value, InputFileType.FASTQ)
                field = value
            }

        @set:Option(
            description = ["Pipe not parsed R1 reads into separate file."],
            names = ["--not-parsed-R1"],
            paramLabel = "<path.fastq[.gz]>"
        )
        var notParsedReadsR1: Path? = null
            set(value) {
                ValidationException.requireFileType(value, InputFileType.FASTQ)
                field = value
            }

        @set:Option(
            description = ["Pipe not parsed R2 reads into separate file."],
            names = ["--not-parsed-R2"],
            paramLabel = "<path.fastq[.gz]>"
        )
        var notParsedReadsR2: Path? = null
            set(value) {
                ValidationException.requireFileType(value, InputFileType.FASTQ)
                field = value
            }
    }

    fun checkInputs(paths: List<Path>) {
        when (paths.size) {
            1 -> ValidationException.requireFileType(
                paths[0],
                InputFileType.FASTQ,
                InputFileType.FASTA,
                InputFileType.BAM
            )

            2 -> {
                ValidationException.requireFileType(paths[0], InputFileType.FASTQ)
                ValidationException.requireFileType(paths[1], InputFileType.FASTQ)
            }

            else -> throw ValidationException("Required 1 or 2 input files, got $paths")
        }
    }

    const val inputsLabel = "(file_R1.fastq[.gz] file_R2.fastq[.gz]|file_RN.(fastq[.gz]|fasta|bam|sam))"

    private const val outputLabel = "alignments.vdjca"

    private const val cellSplitGroupLabel = "CELLSPLIT"

    fun mkCommandSpec(): CommandSpec = CommandSpec.forAnnotatedObject(Cmd::class.java)
        .addPositional(
            PositionalParamSpec.builder()
                .index("0")
                .required(false)
                .arity("0..*")
                .type(Path::class.java)
                .paramLabel(inputsLabel)
                .hideParamSyntax(true)
                .description(
                    "Two fastq files for paired reads or one file for single read data.",
                    "Use {{n}} if you want to concatenate files from multiple lanes, like:",
                    "my_file_L{{n}}_R1.fastq.gz my_file_L{{n}}_R2.fastq.gz"
                )
                .build()
        )
        .addPositional(
            PositionalParamSpec.builder()
                .index("1")
                .required(false)
                .arity("0..*")
                .type(Path::class.java)
                .paramLabel(outputLabel)
                .hideParamSyntax(true)
                .description("Path where to write output alignments")
                .build()
        )

    abstract class CmdBase : MiXCRCommandWithOutputs(), MiXCRPresetAwareCommand<Params> {
        @Option(
            names = ["-O"],
            description = ["Overrides aligner parameters from the selected preset"],
            paramLabel = Labels.OVERRIDES,
            order = 100_000
        )
        private var overrides: Map<String, String> = mutableMapOf()

        @Option(
            description = [
                "Read pre-processing: trimming quality threshold.",
                "Zero value can be used to skip trimming.",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["--trimming-quality-threshold"],
            paramLabel = "<n>"
        )
        private var trimmingQualityThreshold: Byte? = null

        @Option(
            description = [
                "Read pre-processing: trimming window size.",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["--trimming-window-size"],
            paramLabel = "<n>"
        )
        private var trimmingWindowSize: Byte? = null

        @Suppress("unused", "UNUSED_PARAMETER")
        @Option(
            names = ["-c", "--chains"],
            hidden = true
        )
        fun setChains(ignored: String) {
            logger.warn(
                "Don't use --chains option on the alignment step. See --chains parameter in exportAlignments and " +
                        "exportClones actions to limit output to a subset of receptor chains."
            )
        }

        @Option(
            description = [
                "Drop reads from bam file mapped on human chromosomes except with VDJ region (2, 7, 14, 22)",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["--drop-non-vdj"],
            hidden = true
        )
        private var dropNonVDJ = false

        @Option(
            description = [
                "Write alignment results for all input reads (even if alignment failed).",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["--write-all"]
        )
        private var writeAllResults = false

        @Option(
            description = [
                "Read tag pattern from a file.",
                "  Default tag pattern determined by the preset."
            ],
            names = ["--tag-pattern-file"],
            paramLabel = "<path>"
        )
        var tagPatternFile: Path? = null

        @Option(
            description = [
                "If paired-end input is used, determines whether to try all combinations of mate-pairs or " +
                        "only match reads to the corresponding pattern sections (i.e. first file to first section, etc...).",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["--tag-parse-unstranded"]
        )
        private var tagUnstranded = false

        @Option(
            description = [
                "Maximal bit budget, higher values allows more substitutions in small letters.",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["--tag-max-budget"],
            paramLabel = "<n>"
        )
        private var tagMaxBudget: Double? = null

        @Option(
            description = [
                "Marks reads, coming from different files, but having the same positions in those files, " +
                        "as reads coming from the same cells. " +
                        "Main use-case is protocols with overlapped alpha-beta, gamma-delta or heavy-light cDNA molecules, " +
                        "where each side was sequenced by separate mate pairs in a paired-end sequencer. " +
                        "Use special expansion group $cellSplitGroupLabel instead of R index " +
                        "(i.e. \"my_file_R{{$cellSplitGroupLabel:n}}.fastq.gz\").",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["--read-id-as-cell-tag"]
        )
        private var readIdAsCellTag = false

        @Option(
            description = [
                "Copy original reads (sequences + qualities + descriptions) to .vdjca file.",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["-g", "--save-reads"],
            hidden = true
        )
        private var saveReads = false

        @set:Option(
            description = [
                "Maximal number of reads to process",
                DEFAULT_VALUE_FROM_PRESET
            ],
            names = ["-n", "--limit"],
            paramLabel = "<n>",
            hidden = true
        )
        private var limit: Long? = null
            set(value) {
                logger.warn("--limit and -n options are deprecated; use ${LimitInput.CMD_OPTION} instead.")
                field = value
            }

        override val paramsResolver = object : MiXCRParamsResolver<Params>(MiXCRParamsBundle::align) {
            override fun POverridesBuilderOps<Params>.paramsOverrides() {
                if (overrides.isNotEmpty()) {
                    // Printing warning message for some common mistakes in parameter overrides
                    for ((key) in overrides) if ("Parameters.parameters.relativeMinScore" == key.substring(1)) logger.warn(
                        "most probably you want to change \"${key[0]}Parameters.relativeMinScore\" " +
                                "instead of \"${key[0]}Parameters.parameters.relativeMinScore\". " +
                                "The latter should be touched only in a very specific cases."
                    )
                    Params::parameters jsonOverrideWith overrides
                }

                Params::trimmingQualityThreshold setIfNotNull trimmingQualityThreshold
                Params::trimmingWindowSize setIfNotNull trimmingWindowSize
                Params::bamDropNonVDJ setIfTrue dropNonVDJ
                Params::writeFailedAlignments setIfTrue writeAllResults
                Params::tagPattern setIfNotNull tagPatternFile?.readText()
                Params::tagUnstranded setIfTrue tagUnstranded
                Params::tagMaxBudget setIfNotNull tagMaxBudget
                Params::readIdAsCellTag setIfTrue readIdAsCellTag

                if (saveReads)
                    Params::parameters.updateBy {
                        it.setSaveOriginalReads(true)
                    }

                Params::limit setIfNotNull limit
            }

            override fun validateParams(params: Params) {
                if (params.species.isEmpty())
                    throw ValidationException("Species not set, please use -s / --species option to specified it.")
            }
        }
    }

    @Command(
        description = ["Builds alignments with V,D,J and C genes for input sequencing reads."]
    )
    class Cmd : CmdBase() {
        @Option(
            description = ["Analysis preset. Sets all significant parameters of this and all downstream analysis steps. " +
                    "This is a required parameter. It is very important to carefully select the most appropriate preset " +
                    "for the data you analyse."],
            names = ["-p", "--preset"],
            paramLabel = "<name>",
            required = true,
        )
        lateinit var presetName: String

        @Mixin
        var pipelineMixins: PipelineMiXCRMixinsHidden? = null

        @Mixin
        var alignMixins: AlignMiXCRMixins? = null

        @ArgGroup(validate = false, heading = AssembleMiXCRMixins.DESCRIPTION, multiplicity = "0..*")
        var assembleMixins: List<AssembleMiXCRMixins> = mutableListOf()

        @ArgGroup(validate = false, heading = AssembleContigsMiXCRMixins.DESCRIPTION, multiplicity = "0..*")
        var assembleContigsMixins: List<AssembleContigsMiXCRMixins> = mutableListOf()

        @ArgGroup(validate = false, heading = ExportMiXCRMixins.DESCRIPTION, multiplicity = "0..*")
        var exportMixins: List<ExportMiXCRMixins> = mutableListOf()

        @Mixin
        var genericMixins: GenericMiXCRMixins? = null

        private val mixins: MiXCRMixinCollection
            get() = MiXCRMixinCollection.empty + pipelineMixins + alignMixins + assembleMixins +
                    assembleContigsMixins + exportMixins + genericMixins

        @Parameters(
            index = "0",
            arity = "2..3",
            paramLabel = "$inputsLabel $outputLabel",
            hideParamSyntax = true,
            // help is covered by mkCommandSpec
            hidden = true
        )
        private val inOut: List<Path> = mutableListOf()

        private val outputFile get() = inOut.last()

        override val inputFiles get() = inOut.dropLast(1)

        override val outputFiles get() = listOf(outputFile)

        @Option(
            description = ["Size of buffer for FASTQ readers in bytes. Default: 4Mb"],
            names = ["--read-buffer"],
            paramLabel = "<n>"
        )
        var readBufferSize = 1 shl 22 // 4 Mb

        @Mixin
        lateinit var reportOptions: ReportOptions

        @Mixin
        lateinit var threads: ThreadsOption

        @Option(
            description = ["Use higher compression for output file, 10~25%% slower, minus 30~50%% of file size."],
            names = ["--high-compression"]
        )
        var highCompression = false

        @Mixin
        lateinit var pathsForNotAligned: PathsForNotAligned

        @Option(description = ["Show runtime buffer load."], names = ["--buffers"], hidden = true)
        var reportBuffers = false

        private val paramsSpec by lazy { MiXCRParamsSpec(presetName, mixins.mixins) }

        private val bpPair by lazy { paramsResolver.resolve(paramsSpec, printParameters = logger.verbose) }

        private val cmdParams get() = bpPair.second

        val alignerParameters: VDJCAlignerParameters by lazy {
            val parameters = cmdParams.parameters
            // Detect if automatic featureToAlign correction is required
            var totalV = 0
            var totalVErrors = 0
            var hasVRegion = 0
            val correctingFeature = when {
                parameters.vAlignerParameters.geneFeatureToAlign.hasReversedRegions() -> VRegionWithP
                else -> VRegion
            }
            for (gene in vdjcLibrary.getGenes(Chains.parse(cmdParams.chains))) {
                if (gene.geneType == GeneType.Variable) {
                    totalV++
                    if (!parameters.containsRequiredFeature(gene)) {
                        totalVErrors++
                        if (gene.partitioning.isAvailable(correctingFeature)) hasVRegion++
                    }
                }
            }

            // Performing V featureToAlign correction if needed
            if (totalVErrors > totalV * 0.9 && hasVRegion > totalVErrors * 0.8) {
                val currentGenFeature = encode(parameters.vAlignerParameters.geneFeatureToAlign)
                logger.warn(
                    "forcing -OvParameters.geneFeatureToAlign=${encode(correctingFeature)} " +
                            "since current gene feature ($currentGenFeature) is absent in " +
                            "${ReportHelper.PERCENT_FORMAT.format(100.0 * totalVErrors / totalV)}% of V genes."
                )
                parameters.vAlignerParameters.geneFeatureToAlign = correctingFeature
            }
            parameters
        }

        private val vdjcLibrary: VDJCLibrary by lazy {
            val libraryName = libraryNameEnding.matcher(cmdParams.library).replaceAll("")
            VDJCLibraryRegistry.getDefault().getLibrary(libraryName, cmdParams.species)
        }

        /** I.e. list of mate-pair files */
        private val inputFilesExpanded: List<FileGroup> by lazy {
            inputFiles.parseAndRunAndCorrelateFSPattern()
        }

        private val inputHash: String? by lazy {
            LightFileDescriptor.calculateCommutativeLightHash(inputFilesExpanded.map { it.files }.flatten())
                ?.toHexString()
        }

        enum class InputType(val pairedRecords: Boolean, val isFastq: Boolean) {
            SingleEndFastq(false, true),
            PairedEndFastq(true, true),
            Fasta(false, false),
            BAM(true, false)
        }

        private val inputType: InputType by lazy {
            val first = inputFilesExpanded.first().files
            if (first.size == 1) {
                val f0 = first[0]
                when {
                    f0.matches(InputFileType.FASTQ) -> SingleEndFastq
                    f0.matches(InputFileType.FASTA) -> Fasta
                    f0.matches(InputFileType.BAM) -> BAM
                    else -> throw ValidationException("Unknown file type: $f0")
                }
            } else if (first.size == 2) {
                val f0 = first[0]
                val f1 = first[0]
                if (f0.matches(InputFileType.FASTQ) && f0.matches(InputFileType.FASTQ))
                    PairedEndFastq
                else
                    throw ValidationException("Only fastq supports paired end input, can't recognise: $f0 + $f1")
            } else
                throw ValidationException("Too many inputs")
        }

        abstract class FastqGroupReader(fileGroups: List<FileGroup>) :
            OutputPortCloseable<ProcessingBundle>, CanReportProgress {
            private val fileGroupIt = fileGroups.iterator()
            private val readerCount = fileGroups.size
            private var currentReaderIdx = -1

            // -1 used to indicate closed stream
            private var id = 0L
            private var currentReader: SequenceReaderCloseable<SequenceRead>? = null
            private var currentFileTags: List<Pair<String, String>>? = null

            override fun take(): ProcessingBundle? {
                synchronized(fileGroupIt) {
                    if (id == -1L)
                        return null
                    while (true) {
                        currentReader
                            ?.take()
                            ?.let {
                                return ProcessingBundle(
                                    SequenceReadUtil.setReadId(id++, it),
                                    currentFileTags!!,
                                    it.id
                                )
                            }
                        // Terminating the stream if there are no more reads and readers
                        if (!fileGroupIt.hasNext()) {
                            id = -1L
                            return null
                        }
                        // Closing existing reader, if any
                        currentReader?.close()
                        val nextGroup = fileGroupIt.next()
                        // Opening new reader and saving nexct files tags
                        currentReader = nextReader(nextGroup)
                        currentFileTags = nextGroup.tags
                        currentReaderIdx++
                    }
                }
            }

            abstract fun nextReader(fileGroup: FileGroup): SequenceReaderCloseable<SequenceRead>

            override fun close() {
                synchronized(fileGroupIt) {
                    // Closing existing reader, if any
                    currentReader?.close()
                    // Prevents any values to be returned by the stream
                    id = -1L
                }
            }

            override fun getProgress(): Double {
                if (currentReaderIdx == -1)
                    return 0.0
                return (1.0 * currentReaderIdx / readerCount) + (currentReader as CanReportProgress).progress
            }

            override fun isFinished() = id == -1L
        }

        @Suppress("UNCHECKED_CAST")
        private fun createReader(): OutputPortCloseable<ProcessingBundle> {
            // Common single fastq reader constructor
            val fastqReaderFactory: (Path) -> SingleFastqReader = { path: Path ->
                SingleFastqReader(
                    FileInputStream(path.toFile()),
                    SingleFastqReader.DEFAULT_QUALITY_FORMAT,
                    CompressionType.detectCompressionType(path.name),
                    false, readBufferSize,
                    true, true
                )
            }

            return when (inputType) {
                BAM -> {
                    if (inputFilesExpanded.size != 1)
                        throw ValidationException("File concatenation supported only for fastq files.")
                    MiXCRMain.lm.reportApplicationInputs(inputFilesExpanded[0].files)
                    BAMReader(inputFilesExpanded[0].files.toTypedArray(), cmdParams.bamDropNonVDJ, true)
                        .map { ProcessingBundle(it) }

                }

                Fasta -> {
                    if (inputFilesExpanded.size != 1)
                        throw ValidationException("File concatenation supported only for fastq files.")
                    MiXCRMain.lm.reportApplicationInputs(listOf(inputFilesExpanded[0].files[0]))
                    FastaSequenceReaderWrapper(
                        FastaReader(inputFilesExpanded[0].files[0].toFile(), NucleotideSequence.ALPHABET),
                        true
                    )
                        .map { ProcessingBundle(it) }
                }

                SingleEndFastq -> {
                    MiXCRMain.lm.reportApplicationInputs(inputFilesExpanded.map { it.files[0] })
                    object : FastqGroupReader(inputFilesExpanded) {
                        override fun nextReader(fileGroup: FileGroup) =
                            fastqReaderFactory(fileGroup.files[0]) as SequenceReaderCloseable<SequenceRead>
                    }
                }

                PairedEndFastq -> {
                    MiXCRMain.lm.reportApplicationInputs(inputFilesExpanded.map { it.files }.flatten())
                    object : FastqGroupReader(inputFilesExpanded) {
                        override fun nextReader(fileGroup: FileGroup) =
                            PairedFastqReader(
                                fastqReaderFactory(fileGroup.files[0]),
                                fastqReaderFactory(fileGroup.files[1])
                            ) as SequenceReaderCloseable<SequenceRead>
                    }
                }
            }
        }

        override fun inputsMustExist(): Boolean = false

        override fun validate() {
            if (inOut.size > 3) throw ValidationException("Too many input files.")
            if (inOut.size < 2) throw ValidationException("Output file not specified.")

            checkInputs(inputFiles)
            ValidationException.requireFileType(outputFile, InputFileType.VDJCA)

            fun checkFailedReadsOptions(optionPrefix: String, r1: Path?, r2: Path?) {
                if (r1 != null) {
                    when {
                        r2 == null && inputType == PairedEndFastq -> throw ValidationException(
                            "Option ${optionPrefix}-R2 is not specified but paired-end input data provided."
                        )

                        r2 != null && inputType == SingleEndFastq -> throw ValidationException(
                            "Option ${optionPrefix}-R2 is specified but single-end input data provided."
                        )

                        !inputType.isFastq -> throw ValidationException(
                            "Option ${optionPrefix}-* options are supported for fastq data input only."
                        )
                    }
                }
            }
            checkFailedReadsOptions(
                "--not-aligned",
                pathsForNotAligned.notAlignedReadsR1,
                pathsForNotAligned.notAlignedReadsR2
            )
            checkFailedReadsOptions(
                "--not-parsed",
                pathsForNotAligned.notParsedReadsR1,
                pathsForNotAligned.notParsedReadsR2
            )

            if (cmdParams.library.contains("/") || cmdParams.library.contains("\\")) {
                val libraryLocations = Paths.get(
                    System.getProperty("user.home"),
                    ".mixcr",
                    "libraries",
                    "mylibrary.json"
                ).toString()
                throw ValidationException(
                    "Library name can't be a path. Place your library to one of the library search locations " +
                            "(e.g. '$libraryLocations', and put just a library name as -b / --library option value (e.g. '--library mylibrary')."
                )
            }
        }

        /**
         * Alignment report
         */
        private val reportBuilder = AlignerReportBuilder()
        private val qualityTrimmerParameters: QualityTrimmerParameters
            get() = QualityTrimmerParameters(
                cmdParams.trimmingQualityThreshold.toFloat(),
                cmdParams.trimmingWindowSize.toInt()
            )

        override fun run0() {
            // Saving initial timestamp
            val beginTimestamp = System.currentTimeMillis()

            // Printing library level warnings, if specified for the library
            if (!vdjcLibrary.warnings.isEmpty()) {
                logger.warnUnfomatted("Library warnings:")
                for (l in vdjcLibrary.warnings) logger.warnUnfomatted(l)
            }

            // Printing citation notice, if specified for the library
            if (!vdjcLibrary.citations.isEmpty()) {
                logger.warnUnfomatted("Please cite:")
                for (l in vdjcLibrary.citations) logger.warnUnfomatted(l)
            }

            // Tags
            val tagsExtractor = getTagsExtractor()

            // true if final NSQTuple will have two reads, false otherwise
            val pairedPayload =
                if (tagsExtractor.readShortcuts != null) tagsExtractor.readShortcuts.size == 2 else inputType.pairedRecords

            // Creating aligner
            val aligner = VDJCAligner.createAligner(
                alignerParameters,
                pairedPayload,
                cmdParams.overlapPairedReads
            )
            var numberOfExcludedNFGenes = 0
            var numberOfExcludedFGenes = 0
            for (gene in vdjcLibrary.getGenes(Chains.parse(cmdParams.chains))) {
                alignerParameters.getFeatureToAlign(gene.geneType) ?: continue

                val featureSequence = alignerParameters.extractFeatureToAlign(gene)

                var exclusionReason: String? = null
                if (featureSequence == null) exclusionReason =
                    "absent " + encode(alignerParameters.getFeatureToAlign(gene.geneType))
                else if (featureSequence.containsWildcards()) exclusionReason =
                    "wildcard symbols in " + encode(alignerParameters.getFeatureToAlign(gene.geneType))

                // exclusionReason is null ==> gene is not excluded
                if (exclusionReason == null)
                    aligner.addGene(gene) // If there are no reasons to exclude the gene, adding it to aligner
                else {
                    if (gene.isFunctional) {
                        ++numberOfExcludedFGenes
                        if (logger.verbose) logger.warn("Functional gene " + gene.name + " excluded due to " + exclusionReason)
                    } else ++numberOfExcludedNFGenes
                }
            }
            if (numberOfExcludedFGenes > 0) logger.warn(
                "$numberOfExcludedFGenes functional genes were excluded, " +
                        "re-run with --verbose option to see the list of excluded genes and exclusion reason."
            )
            if (logger.verbose && numberOfExcludedNFGenes > 0) logger.warn("$numberOfExcludedNFGenes non-functional genes excluded.")
            if (aligner.vGenesToAlign.isEmpty()) throw ApplicationException(
                "No V genes to align. Aborting execution. See warnings for more info " +
                        "(turn on verbose warnings by adding --verbose option)."
            )
            if (aligner.jGenesToAlign.isEmpty()) throw ApplicationException(
                "No J genes to align. Aborting execution. See warnings for more info " +
                        "(turn on verbose warnings by adding --verbose option)."
            )
            reportBuilder.setStartMillis(beginTimestamp)
            reportBuilder.setInputFiles(inputFiles)
            reportBuilder.setOutputFiles(outputFiles)
            reportBuilder.commandLine = commandLineArguments

            // Attaching report to aligner
            aligner.setEventsListener(reportBuilder)
            use(
                createReader(),
                alignedWriter(outputFile),
                failedReadsWriter(
                    pathsForNotAligned.notAlignedReadsR1,
                    pathsForNotAligned.notAlignedReadsR2
                ),
                failedReadsWriter(
                    pathsForNotAligned.notParsedReadsR1,
                    pathsForNotAligned.notParsedReadsR2
                )
            ) { reader, writer, notAlignedWriter, notParsedWriter ->
                writer?.writeHeader(
                    MiXCRHeader(
                        inputHash,
                        paramsSpec,
                        MiXCRStepParams().add(MiXCRCommandDescriptor.align, cmdParams),
                        TagsInfo(0, *tagsExtractor.tagInfos.toTypedArray()),
                        aligner.parameters,
                        aligner.parameters.featuresToAlignMap,
                        null,
                        null,
                        null
                    ),
                    aligner.usedGenes
                )
                val sReads: OutputPort<ProcessingBundle> = when {
                    cmdParams.limit != null -> CountLimitingOutputPort(reader, cmdParams.limit!!)
                    else -> reader
                }

                val progress: CanReportProgress = when (sReads) {
                    is CountLimitingOutputPort -> SmartProgressReporter.extractProgress(sReads)
                    is CanReportProgress -> sReads
                    else -> throw IllegalArgumentException()
                }

                // Shifting indels in homopolymers is effective only for alignments build with linear gap scoring,
                // consolidating some gaps, on the contrary, for alignments obtained with affine scoring such procedure
                // may break the alignment (gaps there are already consolidated as much as possible)
                val gtRequiringIndelShifts = alignerParameters.geneTypesWithLinearScoring
                val emptyHits = EnumMap<GeneType, Array<VDJCHit?>>(GeneType::class.java)
                for (gt in GeneType.values()) if (alignerParameters.getGeneAlignerParameters(gt) != null) emptyHits[gt] =
                    arrayOfNulls(0)
                val readsLayout = alignerParameters.readsLayout
                SmartProgressReporter.startProgressReport("Alignment", progress)
                val mainInputReads = sReads
                    .chunked(64)
                    .buffered(max(16, threads.value))

                val step0 =
                    mainInputReads.mapUnchunked {
                        val parsed = tagsExtractor.parse(it)
                        if (parsed.status == NotParsed)
                            reportBuilder.onFailedAlignment(VDJCAlignmentFailCause.NoBarcode)
                        parsed
                    }

                val step1 = if (cmdParams.trimmingQualityThreshold > 0) {
                    val rep = ReadTrimmerReportBuilder()
                    val trimmerProcessor = ReadTrimmerProcessor(qualityTrimmerParameters, rep)
                    reportBuilder.setTrimmingReportBuilder(rep)
                    step0.mapUnchunked {
                        it.mapSequence(trimmerProcessor::process)
                    }
                } else
                    step0

                val step2 = step1.mapChunksInParallel(
                    bufferSize = max(16, threads.value),
                    threads = threads.value
                ) {
                    if (it.ok) {
                        var alignment =
                            aligner.process(it.sequence, it.read)?.setTagCount(TagCount(it.tags))
                        if (cmdParams.parameters.isSaveOriginalReads)
                            alignment = alignment?.setOriginalReads(it.read)
                        alignment = alignment?.shiftIndelsAtHomopolymers(gtRequiringIndelShifts)
                        it.copy(
                            alignment = alignment,
                            status = if (alignment == null) NotAligned else Good
                        )
                    } else
                        it
                }

                if (reportBuffers) {
                    checkNotNull(writer)
                    println("Analysis threads: $threads")
                    val reporter = StatusReporter()
                    reporter.addBuffer(
                        "Input (chunked; chunk size = 64)",
                        mainInputReads.bufferStatusProvider
                    )
                    reporter.addBuffer(
                        "Alignment result (chunked; chunk size = 64)",
                        step2.outputBufferStatusProvider
                    )
                    reporter.addCustomProvider(object : StatusProvider {
                        @Suppress("ObjectPropertyName")
                        @Volatile
                        var _status: String = ""

                        @Volatile
                        var isClosed = false
                        override fun updateStatus() {
                            _status = "Busy encoders: " + writer.busyEncoders + " / " + writer.encodersCount
                            isClosed = writer.isClosed
                        }

                        override fun isFinished(): Boolean = isClosed

                        override fun getStatus(): String = _status
                    })
                    reporter.start()
                }

                step2
                    .unchunked()
                    .ordered { it.read.id }
                    .forEach { bundle ->
                        if (bundle.status == NotParsed)
                            notParsedWriter?.write(bundle.read)
                        if (bundle.status == NotAligned)
                            notAlignedWriter?.write(bundle.read)

                        val alignment = when {
                            bundle.alignment != null -> bundle.alignment

                            cmdParams.writeFailedAlignments -> {
                                // Creating empty alignment object if alignment for current read failed
                                val target = readsLayout.createTargets(bundle.sequence)[0]
                                var a = VDJCAlignments(
                                    emptyHits,
                                    if (bundle.tags == TagTuple.NO_TAGS)
                                        TagCount.NO_TAGS_1 else TagCount(bundle.tags),
                                    target.targets,
                                    SequenceHistory.RawSequence.of(bundle.read.id, target),
                                    if (alignerParameters.isSaveOriginalSequence) arrayOf(bundle.sequence) else null
                                )
                                if (alignerParameters.isSaveOriginalReads)
                                    a = a.setOriginalReads(bundle.read)
                                a
                            }

                            else -> return@forEach
                        }

                        if (alignment.isChimera)
                            reportBuilder.onChimera()

                        writer?.write(alignment)
                    }

                writer?.setNumberOfProcessedReads(tagsExtractor.inputReads.get())
                reportBuilder.setFinishMillis(System.currentTimeMillis())
                if (tagsExtractor.reportAgg != null) reportBuilder.tagReportBuilder = tagsExtractor.reportAgg.report
                val report = reportBuilder.buildReport()
                writer?.setFooter(MiXCRFooter().addStepReport(MiXCRCommandDescriptor.align, report))

                // Writing report to stout
                ReportUtil.writeReportToStdout(report)
                reportOptions.appendToFiles(report)
            }
        }

        enum class ProcessingBundleStatus {
            Good,
            NotParsed,
            NotAligned,
        }

        data class ProcessingBundle(
            val read: SequenceRead,
            val fileTags: List<Pair<String, String>> = emptyList(),
            val originalReadId: Long = read.id,
            val sequence: NSQTuple = read.toTuple(),
            val tags: TagTuple = TagTuple.NO_TAGS,
            val alignment: VDJCAlignments? = null,
            val status: ProcessingBundleStatus = Good,
        ) {
            val ok get() = status == Good
            fun mapSequence(mapping: (NSQTuple) -> NSQTuple) = copy(sequence = mapping(sequence))
        }

        @Suppress("UNCHECKED_CAST")
        private fun failedReadsWriter(r1: Path?, r2: Path?): SequenceWriter<SequenceRead>? = when (r1) {
            null -> null
            else -> when (inputType) {
                PairedEndFastq -> PairedFastqWriter(r1.toFile(), r2!!.toFile()) as SequenceWriter<SequenceRead>
                SingleEndFastq -> SingleFastqWriter(r1.toFile()) as SequenceWriter<SequenceRead>
                else -> throw ApplicationException(
                    "Export of reads for which alignment / parsing failed allowed only for fastq inputs."
                ) // must never happen because of parameters validation
            }
        }

        private fun alignedWriter(outputFile: Path) = when (outputFile.toString()) {
            "." -> null
            else -> VDJCAlignmentsWriter(
                outputFile, max(1, threads.value / 8),
                VDJCAlignmentsWriter.DEFAULT_ALIGNMENTS_IN_BLOCK, highCompression
            )
        }

        private val readGroupPattern = Regex("R\\d+")

        private fun getTagsExtractor(): TagsExtractor {
            var plan: ReadSearchPlan? = null
            val readTags = mutableListOf<String>()
            var readTagShortcuts: List<ReadTagShortcut>? = null
            var tagExtractors = mutableListOf<TagExtractorWithInfo>()

            if (cmdParams.tagPattern != null) {
                val searchSettings = ReadSearchSettings(
                    SearchSettings.Default.copy(bitBudget = cmdParams.tagMaxBudget),
                    if (cmdParams.tagUnstranded) ReadSearchMode.DirectAndReversed else ReadSearchMode.Direct
                )
                plan = create(cmdParams.tagPattern!!, searchSettings)
                for (tagName in plan.allTags)
                    if (tagName.matches(readGroupPattern))
                        readTags += tagName
                    else {
                        val type = detectTagTypeByName(tagName) ?: continue
                        tagExtractors += TagExtractorWithInfo(
                            PatternTag(plan.tagShortcut(tagName)),
                            TagInfo(type, TagValueType.SequenceAndQuality, tagName, 0 /* will be changed below */)
                        )
                    }

                readTagShortcuts = readTags.map { name -> plan.tagShortcut(name) }
                if (readTagShortcuts.isEmpty())
                    throw ValidationException("Tag pattern has no read (payload) groups, nothing to align.")
                if (readTagShortcuts.size > 2) throw ValidationException(
                    "Tag pattern contains too many read groups, only R1 or R1+R2 combinations are supported."
                )
            }

            val fileTags = inputFilesExpanded.first().tags.map { it.first }

            if (cmdParams.readIdAsCellTag) {
                if (fileTags != listOf(cellSplitGroupLabel))
                    throw ValidationException(
                        "Exactly one cell splitting group is required in file name for " +
                                "read-id-as-cell-tag feature to work (i.e. \"my_file_R{{$cellSplitGroupLabel:n}}.fastq.gz\")"
                    )
                tagExtractors += TagExtractorWithInfo(
                    ReadIndex,
                    TagInfo(TagType.Cell, TagValueType.NonSequence, "READ_IDX", 0 /* will be changed below */)
                )
            }

            tagExtractors = tagExtractors
                .sortedBy { it.tagInfo }
                .mapIndexed { i, tag -> tag.withInfoIndex(i) }
                .asMutable()

            if (tagExtractors.size != 0) {
                println("The following tags and their roles were recognised:")
                if (readTagShortcuts != null)
                    println("  Payload tags: " + readTags.joinToString(", "))

                tagExtractors
                    .groupBy { it.tagInfo.type }
                    .forEach { (tagType: TagType, extractors: List<TagExtractorWithInfo>) ->
                        println("  $tagType tags: " + extractors.joinToString(", ") { it.tagInfo.name })
                    }
            }

            return TagsExtractor(
                plan, readTagShortcuts,
                emptyList(),
                tagExtractors
                    .sortedBy { it.tagInfo }
                    .mapIndexed { i, tag -> tag.withInfoIndex(i) }
            )
        }

        private sealed interface TagExtractor {
            fun extract(
                originalReadId: Long,
                fileTags: List<Pair<String, String>>,
                headerMatches: List<Matcher>,
                patternMatch: MicRecord?
            ): TagValue
        }

        private data class PatternTag(val shortcut: ReadTagShortcut) : TagExtractor {
            override fun extract(
                originalReadId: Long,
                fileTags: List<Pair<String, String>>,
                headerMatches: List<Matcher>,
                patternMatch: MicRecord?
            ) = SequenceAndQualityTagValue(patternMatch!!.getTagValue(shortcut).value)
        }

        private data class FileTag(val tagName: String) : TagExtractor {
            override fun extract(
                originalReadId: Long,
                fileTags: List<Pair<String, String>>,
                headerMatches: List<Matcher>,
                patternMatch: MicRecord?
            ) = StringTagValue(fileTags.find { it.first == tagName }!!.second)
        }

        private data class HeaderTag(val patternIdx: Int, val groupName: String) : TagExtractor {
            override fun extract(
                originalReadId: Long,
                fileTags: List<Pair<String, String>>,
                headerMatches: List<Matcher>,
                patternMatch: MicRecord?
            ) = StringTagValue(headerMatches[patternIdx].group(groupName))
        }

        private object ReadIndex : TagExtractor {
            override fun extract(
                originalReadId: Long,
                fileTags: List<Pair<String, String>>,
                headerMatches: List<Matcher>,
                patternMatch: MicRecord?
            ) = LongTagValue(originalReadId)
        }

        private data class HeaderPattern(val patter: Pattern, val readIndices: List<Int>?) {
            /** Returns non-null result if all the patterns were matched */
            fun parse(read: SequenceRead): Matcher? {
                for (i in (readIndices ?: (0 until read.numberOfReads()))) {
                    val matcher = patter.matcher(read.getRead(i).description)
                    if (matcher.find())
                        return matcher
                }
                return null
            }
        }

        private data class TagExtractorWithInfo(val tagExtractor: TagExtractor, val tagInfo: TagInfo) {
            fun withInfoIndex(idx: Int) = copy(tagInfo = tagInfo.withIndex(idx))
        }

        private class TagsExtractor(
            /** Not null if tag pattern was specified */
            private val plan: ReadSearchPlan?,
            /** Not null if tag pattern was specified */
            val readShortcuts: List<ReadTagShortcut>?,
            private val headerPatterns: List<HeaderPattern>,
            val tagExtractorsWithInfo: List<TagExtractorWithInfo>,
        ) {
            init {
                require((plan != null) == (readShortcuts != null))
            }

            val tagInfos by lazy { tagExtractorsWithInfo.map { it.tagInfo } }

            val inputReads = AtomicLong()
            val matchedHeaders = AtomicLong()
            val reportAgg = plan?.let { ParseReportAggregator(it) }

            fun parse(bundle: ProcessingBundle): ProcessingBundle {
                inputReads.incrementAndGet()

                val headerMatches = headerPatterns.mapNotNull { it.parse(bundle.read) }
                if (headerMatches.size != headerPatterns.size)
                    return bundle.copy(status = NotParsed)
                matchedHeaders.incrementAndGet()

                val (newSeq, patternMatch) =
                    if (plan != null) {
                        val result = plan.search(bundle.read)
                        reportAgg!!.consume(result)
                        if (result.hit == null) return bundle.copy(status = NotParsed)
                        NSQTuple(
                            bundle.read.id,
                            *Array(readShortcuts!!.size) { i -> result.getTagValue(readShortcuts[i]).value }
                        ) to result
                    } else
                        bundle.sequence to null

                val tags = tagExtractorsWithInfo
                    .map {
                        it.tagExtractor.extract(
                            bundle.originalReadId,
                            bundle.fileTags,
                            headerMatches,
                            patternMatch
                        )
                    }
                    .toTypedArray()

                return bundle.copy(
                    sequence = newSeq,
                    tags = TagTuple(*tags)
                )
            }
        }

        data class ParseInfo(
            val tags: List<TagInfo>,
            val readTags: List<String>
        )

        private fun detectTagTypeByName(name: String): TagType? =
            when {
                name.startsWith("S") -> TagType.Sample
                name.startsWith("CELL") -> TagType.Cell
                name.startsWith("UMI") || name.startsWith("MI") -> TagType.Molecule
                else -> {
                    logger.warn("Can't recognize tag type for name \"$name\", this tag will be ignored during analysis.")
                    null
                }
            }

        private fun parseTagsFromPatternGroupNames(names: Set<String>): ParseInfo {
            val tags: MutableList<TagInfo> = ArrayList()
            val readTags: MutableList<String> = ArrayList()
            for (name in names) {
                when {
                    name.startsWith("S") -> tags += TagInfo(
                        TagType.Sample,
                        TagValueType.SequenceAndQuality,
                        name,
                        0
                    )

                    name.startsWith("CELL") -> tags += TagInfo(
                        TagType.Cell,
                        TagValueType.SequenceAndQuality,
                        name,
                        0
                    )

                    name.startsWith("UMI") || name.startsWith("MI") -> tags += TagInfo(
                        TagType.Molecule,
                        TagValueType.SequenceAndQuality,
                        name,
                        0
                    )

                    name.matches(Regex("R\\d+")) -> readTags += name

                    else -> logger.warn("Can't recognize tag type for name \"$name\", this tag will be ignored during analysis.")
                }
            }
            tags
                .map { it.type }
                .distinct()
                .forEach { tagType -> MiXCRMain.lm.reportFeature("mixcr.tag-type", tagType.toString()) }
            return ParseInfo(
                tags
                    .sorted()
                    .mapIndexed { i, tag -> tag.withIndex(i) },
                readTags.sorted()
            )
        }

        companion object {
            private val libraryNameEnding: Pattern = Pattern.compile("\\.json(?:\\.gz|)$")
        }
    }
}
