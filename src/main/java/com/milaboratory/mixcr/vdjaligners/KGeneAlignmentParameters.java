/*
 * Copyright (c) 2014-2021, MiLaboratories Inc. All Rights Reserved
 *
 * BEFORE DOWNLOADING AND/OR USING THE SOFTWARE, WE STRONGLY ADVISE
 * AND ASK YOU TO READ CAREFULLY LICENSE AGREEMENT AT:
 *
 * https://github.com/milaboratory/mixcr/blob/develop/LICENSE
 */
package com.milaboratory.mixcr.vdjaligners;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.milaboratory.core.alignment.AlignmentScoring;
import com.milaboratory.core.alignment.kaligner1.AbstractKAlignerParameters;
import com.milaboratory.core.alignment.kaligner1.KAlignerParameters;
import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.core.GeneFeature;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class KGeneAlignmentParameters extends GeneAlignmentParametersAbstract<KGeneAlignmentParameters>
        implements java.io.Serializable {
    private AbstractKAlignerParameters parameters;
    private int minSumScore;

    @JsonCreator
    public KGeneAlignmentParameters(
            @JsonProperty("geneFeatureToAlign") GeneFeature geneFeatureToAlign,
            @JsonProperty("minSumScore") Integer minSumScore,
            @JsonProperty("relativeMinScore") Float relativeMinScore,
            @JsonProperty("parameters") AbstractKAlignerParameters parameters) {
        super(geneFeatureToAlign, relativeMinScore  == null ? 0.87f : relativeMinScore.floatValue());
        this.minSumScore = minSumScore == null ? 40 : minSumScore.intValue();
        this.parameters = parameters;
    }

    public int getMinSumScore() {
        return minSumScore;
    }

    public KGeneAlignmentParameters setMinSumScore(int minSumScore) {
        this.minSumScore = minSumScore;
        return this;
    }

    public AbstractKAlignerParameters getParameters() {
        return parameters;
    }

    public KGeneAlignmentParameters setParameters(KAlignerParameters parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    public KGeneAlignmentParameters clone() {
        return new KGeneAlignmentParameters(geneFeatureToAlign, minSumScore, relativeMinScore, parameters.clone());
    }

    @Override
    public AlignmentScoring<NucleotideSequence> getScoring() {
        return parameters.getScoring();
    }

    @Override
    public String toString() {
        return "KGeneAlignmentParameters{" +
                "parameters=" + parameters +
                ", minSumScore=" + minSumScore +
                ", relativeMinScore=" + relativeMinScore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        KGeneAlignmentParameters that = (KGeneAlignmentParameters) o;

        if (minSumScore != that.minSumScore) return false;
        return parameters != null ? parameters.equals(that.parameters) : that.parameters == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + minSumScore;
        return result;
    }
}
