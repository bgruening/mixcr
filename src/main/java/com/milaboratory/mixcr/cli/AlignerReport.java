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
package com.milaboratory.mixcr.cli;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.quality.ReadTrimmerReport;
import com.milaboratory.mitool.report.ParseReport;
import com.milaboratory.mixcr.basictypes.VDJCAlignments;
import com.milaboratory.mixcr.vdjaligners.VDJCAlignerEventListener;
import com.milaboratory.mixcr.vdjaligners.VDJCAlignmentFailCause;
import com.milaboratory.util.ReportHelper;
import io.repseq.core.GeneType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public final class AlignerReport extends AbstractCommandReport implements VDJCAlignerEventListener {
    private final ChainUsageStats chainStats = new ChainUsageStats();
    private final AtomicLongArray fails = new AtomicLongArray(VDJCAlignmentFailCause.values().length);
    private final AtomicLong successes = new AtomicLong(0);
    // private final AtomicLong droppedNoBarcode = new AtomicLong(0);
    // private final AtomicLong droppedBarcodeNotInWhitelist = new AtomicLong(0);
    private final AtomicLong chimeras = new AtomicLong(0);
    private final AtomicLong alignedSequenceOverlap = new AtomicLong(0);
    private final AtomicLong alignedAlignmentOverlap = new AtomicLong(0);
    private final AtomicLong noCDR3PartsAlignments = new AtomicLong(0);
    private final AtomicLong partialAlignments = new AtomicLong(0);
    private final AtomicLong nonAlignedOverlap = new AtomicLong(0);
    private final AtomicLong topHitConflict = new AtomicLong(0);
    private final AtomicLong vChimeras = new AtomicLong(0);
    private final AtomicLong jChimeras = new AtomicLong(0);
    private final AtomicLong realignedWithForcedNonFloatingBound = new AtomicLong(0);
    private final AtomicLong realignedWithForcedNonFloatingRightBoundInLeftRead = new AtomicLong(0);
    private final AtomicLong realignedWithForcedNonFloatingLeftBoundInRightRead = new AtomicLong(0);
    private ReadTrimmerReport trimmingReport;

    private ParseReport tagReport;

    public AlignerReport() {
    }

    @Override
    public String getCommand() {
        return "align";
    }

    public long getFails(VDJCAlignmentFailCause cause) {
        return fails.get(cause.ordinal());
    }

    @JsonProperty("trimmingReport")
    public ReadTrimmerReport getTrimmingReport() {
        return trimmingReport;
    }

    public void setTrimmingReport(ReadTrimmerReport trimmingReport) {
        this.trimmingReport = trimmingReport;
    }

    // @JsonProperty("trimmingReport")
    public ParseReport getTagReport() {
        return tagReport;
    }

    public void setTagReport(ParseReport tagReport) {
        this.tagReport = tagReport;
    }

    @JsonProperty("totalReadsProcessed")
    public long getTotal() {
        long total = 0;
        for (int i = 0; i < fails.length(); ++i)
            total += fails.get(i);
        total += successes.get();
        return total;
    }

    @JsonProperty("aligned")
    public long getSuccess() {
        return successes.get();
    }

    @JsonProperty("notAligned")
    public long getNonAlignedTotal() {
        long val = 0;
        for (int i = 0; i < fails.length(); i++)
            val += fails.get(i);
        return val;
    }

    @JsonProperty("notAlignedReasons")
    public Map<String, Long> getFailsMap() {
        Map<String, Long> map = new HashMap<>();
        for (VDJCAlignmentFailCause cause : VDJCAlignmentFailCause.values())
            map.put(cause.toString(), fails.get(cause.ordinal()));
        return map;
    }

    @JsonProperty("chimeras")
    public long getChimeras() {
        return chimeras.get();
    }

    @JsonProperty("overlapped")
    public long getOverlapped() {
        return getAlignedOverlaps() + nonAlignedOverlap.get();
    }

    public long getFailsNoVHits() {
        return getFails(VDJCAlignmentFailCause.NoVHits);
    }

    public long getFailsNoJHits() {
        return getFails(VDJCAlignmentFailCause.NoJHits);
    }

    public long getFailsLowTotalScore() {
        return getFails(VDJCAlignmentFailCause.LowTotalScore);
    }

    public long getSuccesses() {
        return successes.get();
    }

    // @JsonProperty("droppedNoBarcode")
    // public long getDroppedNoBarcode() {
    //     return droppedNoBarcode.get();
    // }
    //
    // @JsonProperty("droppedBarcodeNotInWhitelist")
    // public long getDroppedBarcodeNotInWhitelist() {
    //     return droppedBarcodeNotInWhitelist.get();
    // }

    @JsonProperty("alignmentAidedOverlaps")
    public long getAlignmentOverlaps() {
        return alignedAlignmentOverlap.get();
    }

    @JsonProperty("overlappedAligned")
    public long getAlignedOverlaps() {
        return alignedSequenceOverlap.get() + alignedAlignmentOverlap.get();
    }

    @JsonProperty("overlappedNotAligned")
    public long getNonAlignedOverlaps() {
        return nonAlignedOverlap.get();
    }

    @JsonProperty("pairedEndAlignmentConflicts")
    public long getTopHitSequenceConflicts() {
        return topHitConflict.get();
    }

    @JsonProperty("vChimeras")
    public long getVChimeras() {
        return vChimeras.get();
    }

    @JsonProperty("jChimeras")
    public long getJChimeras() {
        return jChimeras.get();
    }

    @JsonProperty("chainUsage")
    public ChainUsageStats getChainUsage() {
        return chainStats;
    }

    @JsonProperty("realignedWithForcedNonFloatingBound")
    public long getRealignedWithForcedNonFloatingBound() {
        return realignedWithForcedNonFloatingBound.get();
    }

    @JsonProperty("realignedWithForcedNonFloatingRightBoundInLeftRead")
    public long getRealignedWithForcedNonFloatingRightBoundInLeftRead() {
        return realignedWithForcedNonFloatingRightBoundInLeftRead.get();
    }

    @JsonProperty("realignedWithForcedNonFloatingLeftBoundInRightRead")
    public long getRealignedWithForcedNonFloatingLeftBoundInRightRead() {
        return realignedWithForcedNonFloatingLeftBoundInRightRead.get();
    }

    // public void onNoBarcode(SequenceRead read) {
    //     droppedNoBarcode.incrementAndGet();
    // }
    //
    // public void onBarcodeNotInWhitelist(SequenceRead read) {
    //     droppedBarcodeNotInWhitelist.incrementAndGet();
    // }

    @Override
    public void onFailedAlignment(SequenceRead read, VDJCAlignmentFailCause cause) {
        fails.incrementAndGet(cause.ordinal());
    }

    @Override
    public void onSuccessfulAlignment(SequenceRead read, VDJCAlignments alignment) {
        successes.incrementAndGet();
        chainStats.increment(alignment);
    }

    @Override
    public void onSuccessfulSequenceOverlap(SequenceRead read, VDJCAlignments alignments) {
        if (alignments == null)
            nonAlignedOverlap.incrementAndGet();
        else
            alignedSequenceOverlap.incrementAndGet();
    }

    @Override
    public void onSuccessfulAlignmentOverlap(SequenceRead read, VDJCAlignments alignments) {
        if (alignments == null)
            throw new IllegalArgumentException();
        alignedAlignmentOverlap.incrementAndGet();
    }

    @Override
    public void onTopHitSequenceConflict(SequenceRead read, VDJCAlignments alignments, GeneType geneType) {
        topHitConflict.incrementAndGet();
    }

    @Override
    public void onSegmentChimeraDetected(GeneType geneType, SequenceRead read, VDJCAlignments alignments) {
        switch (geneType) {
            case Variable:
                vChimeras.incrementAndGet();
                return;
            case Joining:
                jChimeras.incrementAndGet();
                return;
            default:
                throw new IllegalArgumentException(geneType.toString());
        }
    }

    public void onChimera() {
        chimeras.incrementAndGet();
    }

    @JsonProperty("noCDR3PartsAlignments")
    public long getNoCDR3PartsAlignments() {
        return noCDR3PartsAlignments.get();
    }

    @JsonProperty("partialAlignments")
    public long getPartialAlignments() {
        return partialAlignments.get();
    }

    @Override
    public void onNoCDR3PartsAlignment() {
        noCDR3PartsAlignments.incrementAndGet();
    }

    @Override
    public void onPartialAlignment() {
        partialAlignments.incrementAndGet();
    }

    @Override
    public void onRealignmentWithForcedNonFloatingBound(boolean forceLeftEdgeInRight, boolean forceRightEdgeInLeft) {
        realignedWithForcedNonFloatingBound.getAndIncrement();
        if (forceRightEdgeInLeft)
            realignedWithForcedNonFloatingRightBoundInLeftRead.incrementAndGet();
        if (forceRightEdgeInLeft)
            realignedWithForcedNonFloatingLeftBoundInRightRead.incrementAndGet();
    }

    @Override
    public void writeReport(ReportHelper helper) {
        // Writing common analysis information
        writeSuperReport(helper);

        long total = getTotal();
        long success = getSuccess();
        helper.writeField("Total sequencing reads", total);
        helper.writePercentAndAbsoluteField("Successfully aligned reads", success, total);

        // if (getDroppedBarcodeNotInWhitelist() != 0 || getDroppedNoBarcode() != 0) {
        //     helper.writePercentAndAbsoluteField("Absent barcode", getDroppedNoBarcode(), total);
        //     helper.writePercentAndAbsoluteField("Barcode not in whitelist", getDroppedBarcodeNotInWhitelist(), total);
        // }

        if (getChimeras() != 0)
            helper.writePercentAndAbsoluteField("Chimeras", getChimeras(), total);

        if (getTopHitSequenceConflicts() != 0)
            helper.writePercentAndAbsoluteField("Paired-end alignment conflicts eliminated", getTopHitSequenceConflicts(), total);

        for (VDJCAlignmentFailCause cause : VDJCAlignmentFailCause.values())
            if (fails.get(cause.ordinal()) != 0)
                helper.writePercentAndAbsoluteField(cause.reportLine, fails.get(cause.ordinal()), total);

        helper.writePercentAndAbsoluteField("Overlapped", getOverlapped(), total);
        helper.writePercentAndAbsoluteField("Overlapped and aligned", getAlignedOverlaps(), total);
        helper.writePercentAndAbsoluteField("Alignment-aided overlaps", getAlignmentOverlaps(), getAlignedOverlaps());
        helper.writePercentAndAbsoluteField("Overlapped and not aligned", getNonAlignedOverlaps(), total);
        helper.writePercentAndAbsoluteFieldNonZero("No CDR3 parts alignments, percent of successfully aligned", getNoCDR3PartsAlignments(), success);
        helper.writePercentAndAbsoluteFieldNonZero("Partial aligned reads, percent of successfully aligned", getPartialAlignments(), success);

        if (getVChimeras() != 0)
            helper.writePercentAndAbsoluteField("V gene chimeras", getVChimeras(), total);

        if (getJChimeras() != 0)
            helper.writePercentAndAbsoluteField("J gene chimeras", getJChimeras(), total);

        // Writing distribution by chains
        chainStats.writeReport(helper);

        helper.writePercentAndAbsoluteField("Realigned with forced non-floating bound", getRealignedWithForcedNonFloatingBound(), total);
        helper.writePercentAndAbsoluteField("Realigned with forced non-floating right bound in left read", getRealignedWithForcedNonFloatingRightBoundInLeftRead(), total);
        helper.writePercentAndAbsoluteField("Realigned with forced non-floating left bound in right read", getRealignedWithForcedNonFloatingLeftBoundInRightRead(), total);

        if (trimmingReport != null) {
            // assert trimmingReport.getAlignments() == total;
            helper.writePercentAndAbsoluteField("R1 Reads Trimmed Left", trimmingReport.getR1LeftTrimmedEvents(), total);
            helper.writePercentAndAbsoluteField("R1 Reads Trimmed Right", trimmingReport.getR1RightTrimmedEvents(), total);
            helper.writeField("Average R1 Nucleotides Trimmed Left", 1.0 * trimmingReport.getR1LeftTrimmedNucleotides() / total);
            helper.writeField("Average R1 Nucleotides Trimmed Right", 1.0 * trimmingReport.getR1RightTrimmedNucleotides() / total);
            if (trimmingReport.getR2LeftTrimmedEvents() > 0 || trimmingReport.getR2RightTrimmedEvents() > 0) {
                helper.writePercentAndAbsoluteField("R2 Reads Trimmed Left", trimmingReport.getR2LeftTrimmedEvents(), total);
                helper.writePercentAndAbsoluteField("R2 Reads Trimmed Right", trimmingReport.getR2RightTrimmedEvents(), total);
                helper.writeField("Average R2 Nucleotides Trimmed Left", 1.0 * trimmingReport.getR2LeftTrimmedNucleotides() / total);
                helper.writeField("Average R2 Nucleotides Trimmed Right", 1.0 * trimmingReport.getR2RightTrimmedNucleotides() / total);
            }
        }

        if (tagReport != null)
            tagReport.writeReport(helper);
    }
}
