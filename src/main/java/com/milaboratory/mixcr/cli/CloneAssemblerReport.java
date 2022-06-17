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
import com.milaboratory.mixcr.assembler.CloneAccumulator;
import com.milaboratory.mixcr.assembler.CloneAssemblerListener;
import com.milaboratory.mixcr.assembler.preclone.PreClone;
import com.milaboratory.mixcr.assembler.preclone.PreCloneAssemblerReport;
import com.milaboratory.mixcr.basictypes.Clone;
import com.milaboratory.mixcr.basictypes.CloneSet;
import com.milaboratory.util.ReportHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class CloneAssemblerReport extends AbstractCommandReport implements CloneAssemblerListener {
    private final ChainUsageStats chainStats = new ChainUsageStats();
    private PreCloneAssemblerReport preCloneAssemblerReport;
    long totalReads = -1;
    final AtomicInteger clonesCreated = new AtomicInteger();
    final AtomicLong failedToExtractTarget = new AtomicLong();
    final AtomicLong droppedAsLowQuality = new AtomicLong();
    final AtomicLong alignmentsInClones = new AtomicLong();
    final AtomicLong coreAlignments = new AtomicLong();
    final AtomicLong deferred = new AtomicLong();
    final AtomicLong deferredAlignmentsDropped = new AtomicLong();
    final AtomicLong deferredAlignmentsMapped = new AtomicLong();
    final AtomicInteger clonesClustered = new AtomicInteger();
    final AtomicInteger clonesDropped = new AtomicInteger();
    final AtomicInteger clonesDroppedInFineFiltering = new AtomicInteger();
    final AtomicLong readsDroppedWithClones = new AtomicLong();
    final AtomicInteger clonesPreClustered = new AtomicInteger();
    final AtomicLong readsPreClustered = new AtomicLong();
    final AtomicLong readsClustered = new AtomicLong();
    final AtomicLong readsAttachedByTags = new AtomicLong();
    final AtomicLong readsFailedToAttachedByTags = new AtomicLong();
    final AtomicLong readsWithAmbiguousAttachmentsByTags = new AtomicLong();

    @Override
    public String getCommand() {
        return "assemble";
    }

    // TODO 4.0
    // @JsonProperty("preCloneAssemblerReport")
    public PreCloneAssemblerReport getPreCloneAssemblerReport() {
        return preCloneAssemblerReport;
    }

    public void setPreCloneAssemblerReport(PreCloneAssemblerReport preCloneAssemblerReport) {
        if (this.preCloneAssemblerReport != null)
            throw new IllegalStateException("Pre-clone assembler report already set.");
        this.preCloneAssemblerReport = preCloneAssemblerReport;
    }

    @JsonProperty("totalReadsProcessed")
    public long getTotalReads() {
        return totalReads;
    }

    @JsonProperty("initialClonesCreated")
    public int getClonesCreated() {
        return clonesCreated.get();
    }

    @JsonProperty("readsDroppedNoTargetSequence")
    public long getFailedToExtractTarget() {
        return failedToExtractTarget.get();
    }

    @JsonProperty("readsDroppedLowQuality")
    public long getDroppedAsLowQuality() {
        return droppedAsLowQuality.get();
    }

    public long getDeferred() {
        return deferred.get();
    }

    @JsonProperty("coreReads")
    public long getCoreAlignments() {
        return coreAlignments.get();
    }

    @JsonProperty("readsDroppedFailedMapping")
    public long getDeferredAlignmentsDropped() {
        return deferredAlignmentsDropped.get();
    }

    @JsonProperty("lowQualityRescued")
    public long getDeferredReadsMapped() {
        return deferredAlignmentsMapped.get();
    }

    @JsonProperty("clonesClustered")
    public int getClonesClustered() {
        return clonesClustered.get();
    }

    @JsonProperty("readsClustered")
    public long getReadsClustered() {
        return readsClustered.get();
    }

    @JsonProperty("clones")
    public int getCloneCount() {
        return clonesCreated.get() - clonesClustered.get() - clonesDropped.get() - clonesPreClustered.get();
    }

    @JsonProperty("clonesDroppedAsLowQuality")
    public int getClonesDropped() {
        return clonesDropped.get();
    }

    @JsonProperty("clonesDroppedInFineFiltering")
    public int getClonesDroppedInFineFiltering() {
        return clonesDroppedInFineFiltering.get();
    }

    @JsonProperty("clonesPreClustered")
    public int getClonesPreClustered() {
        return clonesPreClustered.get();
    }

    @JsonProperty("readsPreClustered")
    public long getReadsPreClustered() {
        return readsPreClustered.get();
    }

    @JsonProperty("readsInClones")
    public long getReadsInClones() {
        return alignmentsInClones.get(); //coreAlignments.get() + deferredAlignmentsMapped.get() - readsDroppedWithClones.get();
    }

    @JsonProperty("readsInClonesBeforeClustering")
    public long getReadsInClonesBeforeClustering() {
        return deferredAlignmentsMapped.get() + coreAlignments.get();
    }

    @JsonProperty("readsDroppedWithLowQualityClones")
    public long getReadsDroppedWithClones() {
        return readsDroppedWithClones.get();
    }

    @JsonProperty("clonalChainUsage")
    public ChainUsageStats getClonalChainUsage() {
        return chainStats;
    }

    @JsonProperty("readsAttachedByTags")
    public long getReadsAttachedByTags() {
        return readsAttachedByTags.get();
    }

    @JsonProperty("readsWithAmbiguousAttachmentsByTags")
    public long getReadsWithAmbiguousAttachmentsByTags() {
        return readsWithAmbiguousAttachmentsByTags.get();
    }

    @JsonProperty("readsFailedToAttachedByTags")
    public long getReadsFailedToAttachedByTags() {
        return readsFailedToAttachedByTags.get();
    }

    @Override
    public void onNewCloneCreated(CloneAccumulator accumulator) {
        clonesCreated.incrementAndGet();
    }

    @Override
    public void onFailedToExtractTarget(PreClone preClone) {
        failedToExtractTarget.addAndGet(preClone.getNumberOfReads());
    }

    @Override
    public void onTooManyLowQualityPoints(PreClone preClone) {
        droppedAsLowQuality.addAndGet(preClone.getNumberOfReads());
    }

    @Override
    public void onAlignmentDeferred(PreClone preClone) {
        deferred.addAndGet(preClone.getNumberOfReads());
    }

    @Override
    public void onAlignmentAddedToClone(PreClone preClone, CloneAccumulator accumulator) {
        coreAlignments.addAndGet(preClone.getNumberOfReads());
        alignmentsInClones.addAndGet(preClone.getNumberOfReads());
    }

    @Override
    public void onNoCandidateFoundForDeferredAlignment(PreClone preClone) {
        deferredAlignmentsDropped.addAndGet(preClone.getNumberOfReads());
    }

    @Override
    public void onDeferredAlignmentMappedToClone(PreClone preClone, CloneAccumulator accumulator) {
        deferredAlignmentsMapped.addAndGet(preClone.getNumberOfReads());
        alignmentsInClones.addAndGet(preClone.getNumberOfReads());
    }

    @Override
    public void onClustered(CloneAccumulator majorClone, CloneAccumulator minorClone, boolean countAdded) {
        readsClustered.addAndGet(minorClone.getCount());
        clonesClustered.incrementAndGet();
        if (!countAdded)
            alignmentsInClones.addAndGet(-minorClone.getCount());
    }

    @Override
    public void onPreClustered(CloneAccumulator majorClone, CloneAccumulator minorClone) {
        clonesPreClustered.incrementAndGet();
        readsPreClustered.addAndGet(minorClone.getCount());
    }

    @Override
    public void onCloneDropped(CloneAccumulator clone) {
        readsDroppedWithClones.addAndGet(clone.getCount());
        clonesDropped.incrementAndGet();
        alignmentsInClones.addAndGet(-clone.getCount());
        coreAlignments.addAndGet(-clone.getCoreCount());
        deferredAlignmentsMapped.addAndGet(-clone.getMappedCount());
        deferred.addAndGet(-clone.getMappedCount());
    }

    @Override
    public void onCloneDroppedInFineFiltering(CloneAccumulator clone) {
        onCloneDropped(clone);
        clonesDroppedInFineFiltering.incrementAndGet();
    }

    public void onClonesetFinished(CloneSet cloneSet) {
        for (Clone clone : cloneSet)
            chainStats.increment(clone);
    }

    public void setTotalReads(long totalReads) {
        this.totalReads = totalReads;
    }

    public void onReadAttachedByTags() {
        readsAttachedByTags.incrementAndGet();
    }

    public void onReadWithAmbiguousAttachmentsByTags() {
        readsWithAmbiguousAttachmentsByTags.incrementAndGet();
    }

    public void onReadsFailedToAttachedByTags() {
        readsFailedToAttachedByTags.incrementAndGet();
    }

    @Override
    public void writeReport(ReportHelper helper) {
        // Writing common analysis information
        writeSuperReport(helper);

        // Writing pre-clone assembler report (should be present for barcoded analysis)
        if (preCloneAssemblerReport != null)
            preCloneAssemblerReport.writeReport(helper);

        if (totalReads == -1)
            throw new IllegalStateException("TotalReads count not set.");

        int clonesCount = getCloneCount();

        long alignmentsInClones = getReadsInClones();

        // Alignments in clones before clusterization
        long clusterizationBase = getReadsInClonesBeforeClustering();

        helper.writeField("Final clonotype count", clonesCount)
                .writeField("Average number of reads per clonotype", ReportHelper.PERCENT_FORMAT.format(1.0 * alignmentsInClones / clonesCount))
                .writePercentAndAbsoluteField("Reads used in clonotypes, percent of total", alignmentsInClones, totalReads)
                .writePercentAndAbsoluteField("Reads used in clonotypes before clustering, percent of total", clusterizationBase, totalReads)
                .writePercentAndAbsoluteField("Number of reads used as a core, percent of used", coreAlignments.get(), clusterizationBase)
                .writePercentAndAbsoluteField("Mapped low quality reads, percent of used", deferredAlignmentsMapped.get(), clusterizationBase)
                .writePercentAndAbsoluteField("Reads clustered in PCR error correction, percent of used", readsClustered.get(), clusterizationBase)
                .writePercentAndAbsoluteField("Reads pre-clustered due to the similar VJC-lists, percent of used", readsPreClustered.get(), alignmentsInClones)
                .writePercentAndAbsoluteField("Reads dropped due to the lack of a clone sequence, percent of total",
                        failedToExtractTarget.get(), totalReads)
                .writePercentAndAbsoluteField("Reads dropped due to low quality, percent of total",
                        droppedAsLowQuality.get(), totalReads)
                .writePercentAndAbsoluteField("Reads dropped due to failed mapping, percent of total",
                        deferredAlignmentsDropped.get(), totalReads)
                .writePercentAndAbsoluteField("Reads dropped with low quality clones, percent of total", readsDroppedWithClones.get(), totalReads)
                .writeField("Clonotypes eliminated by PCR error correction", clonesClustered.get())
                .writeField("Clonotypes dropped as low quality", clonesDropped.get())
                .writeField("Clonotypes pre-clustered due to the similar VJC-lists", clonesPreClustered.get())
                .writeField("Clonotypes dropped in fine filtering", clonesDroppedInFineFiltering.get())
                .writePercentAndAbsoluteField("Partially aligned reads attached to clones by tags", readsAttachedByTags.get(), totalReads)
                .writePercentAndAbsoluteField("Partially aligned reads with ambiguous clone attachments by tags", readsWithAmbiguousAttachmentsByTags.get(), totalReads)
                .writePercentAndAbsoluteField("Partially aligned reads failed to attach to clones by tags", readsFailedToAttachedByTags.get(), totalReads);
        ;

        // Writing distribution by chains
        chainStats.writeReport(helper);
    }
}
