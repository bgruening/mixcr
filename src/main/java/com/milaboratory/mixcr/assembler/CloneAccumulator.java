/*
 * Copyright (c) 2014-2021, MiLaboratories Inc. All Rights Reserved
 *
 * BEFORE DOWNLOADING AND/OR USING THE SOFTWARE, WE STRONGLY ADVISE
 * AND ASK YOU TO READ CAREFULLY LICENSE AGREEMENT AT:
 *
 * https://github.com/milaboratory/mixcr/blob/develop/LICENSE
 */
package com.milaboratory.mixcr.assembler;


import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.core.sequence.quality.QualityAggregationType;
import com.milaboratory.core.sequence.quality.QualityAggregator;
import com.milaboratory.mixcr.basictypes.ClonalSequence;
import com.milaboratory.mixcr.basictypes.VDJCAlignments;
import com.milaboratory.mixcr.basictypes.VDJCHit;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.hash.TObjectFloatHashMap;
import io.repseq.core.GeneType;
import io.repseq.core.VDJCGeneId;

import java.util.EnumMap;

public final class CloneAccumulator {
    final EnumMap<GeneType, TObjectFloatHashMap<VDJCGeneId>> geneScores = new EnumMap<>(GeneType.class);
    private ClonalSequence sequence;
    private final QualityAggregator aggregator;
    private long coreCount = 0, mappedCount = 0, initialCoreCount = -1;
    private volatile int cloneIndex = -1;
    final Range[] nRegions;

    public CloneAccumulator(ClonalSequence sequence, Range[] nRegions, QualityAggregationType qualityAggregationType) {
        this.sequence = sequence;
        this.nRegions = nRegions;
        this.aggregator = qualityAggregationType.create(sequence.getConcatenated().size());
        //this.quality = sequence.getConcatenated().getQuality().asArray();
    }

    public ClonalSequence getSequence() {
        return sequence;
    }

    public void rebuildClonalSequence() {
        SequenceQuality newQuality = aggregator.getQuality();
        final NSequenceWithQuality[] updated = new NSequenceWithQuality[sequence.size()];
        int pointer = 0;
        for (int i = 0; i < updated.length; i++) {
            final NucleotideSequence s = this.sequence.get(i).getSequence();
            updated[i] = new NSequenceWithQuality(s, newQuality.getRange(pointer, pointer + s.size()));
            pointer += s.size();
        }
        sequence = new ClonalSequence(updated);
        return;
    }

    public void onBeforeMapping() {
        initialCoreCount = coreCount;
    }

    public float getBestScore(GeneType geneType) {
        TObjectFloatHashMap<VDJCGeneId> scores = geneScores.get(geneType);
        if (scores == null)
            return 0;
        float maxScore = 0;
        TObjectFloatIterator<VDJCGeneId> iterator = scores.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            if (maxScore < iterator.value())
                maxScore = iterator.value();
        }
        return maxScore;
    }

    public VDJCGeneId getBestGene(GeneType geneType) {
        TObjectFloatHashMap<VDJCGeneId> scores = geneScores.get(geneType);
        if (scores == null)
            return null;
        float maxScore = 0;
        VDJCGeneId maxAllele = null;
        TObjectFloatIterator<VDJCGeneId> iterator = scores.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            if (maxAllele == null || maxScore < iterator.value()) {
                maxAllele = iterator.key();
                maxScore = iterator.value();
            }
        }
        return maxAllele;
    }

    public Range[] getNRegions() {
        return nRegions;
    }

    public void setCloneIndex(int cloneIndex) {
        this.cloneIndex = cloneIndex;
    }

    public int getCloneIndex() {
        // O_o
        while (cloneIndex == -1) ;
        return cloneIndex;
    }

    public long getInitialCoreCount() {
        return initialCoreCount;
    }

    public long getCount() {
        return coreCount + mappedCount;
    }

    public long getCoreCount() {
        return coreCount;
    }

    public long getMappedCount() {
        return mappedCount;
    }

    public void calculateScores(CloneFactoryParameters parameters) {
        for (GeneType geneType : GeneType.VJC_REFERENCE) {
            VJCClonalAlignerParameters vjcParameters = parameters.getVJCParameters(geneType);
            if (vjcParameters == null)
                continue;

            TObjectFloatHashMap<VDJCGeneId> accumulatorGeneIds = geneScores.get(geneType);
            if (accumulatorGeneIds == null)
                continue;

            TObjectFloatIterator<VDJCGeneId> iterator = accumulatorGeneIds.iterator();
            float maxScore = 0;
            while (iterator.hasNext()) {
                iterator.advance();
                float value = iterator.value();
                if (value > maxScore)
                    maxScore = value;
            }

            maxScore = maxScore * vjcParameters.getRelativeMinScore();
            iterator = accumulatorGeneIds.iterator();
            while (iterator.hasNext()) {
                iterator.advance();
                if (maxScore > iterator.value())
                    iterator.remove();
                else
                    iterator.setValue(Math.round(iterator.value() * 10f / coreCount) / 10f);
            }
        }
    }

    public void mergeCounts(CloneAccumulator acc) {
        coreCount += acc.coreCount;
        mappedCount += acc.mappedCount;
    }

    public synchronized void accumulate(ClonalSequence data, VDJCAlignments alignment, boolean mapped) {
        if (!mapped) { // Core sequence accumulation
            coreCount += alignment.getNumberOfReads();

            // Accumulate information about V-D-J alignments only for strictly clustered reads
            // (only for core clonotypes members)
            float score;

            // Accumulate information about all genes
            for (GeneType geneType : GeneType.VJC_REFERENCE) {
                TObjectFloatHashMap<VDJCGeneId> geneScores = this.geneScores.get(geneType);
                VDJCHit[] hits = alignment.getHits(geneType);
                if (hits.length == 0)
                    continue;
                if (geneScores == null)
                    this.geneScores.put(geneType, geneScores = new TObjectFloatHashMap<>());
                for (VDJCHit hit : hits) {
                    // Calculating sum of natural logarithms of scores
                    score = hit.getScore();
                    geneScores.adjustOrPutValue(hit.getGene().getId(), score, score);
                }
            }

            aggregator.aggregate(data.getConcatenated().getQuality());
        } else // Mapped sequence accumulation
            mappedCount += alignment.getNumberOfReads();
    }
}
