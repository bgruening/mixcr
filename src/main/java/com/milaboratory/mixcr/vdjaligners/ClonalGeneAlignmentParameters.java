/*
 * Copyright (c) 2014-2021, MiLaboratories Inc. All Rights Reserved
 *
 * BEFORE DOWNLOADING AND/OR USING THE SOFTWARE, WE STRONGLY ADVISE
 * AND ASK YOU TO READ CAREFULLY LICENSE AGREEMENT AT:
 *
 * https://github.com/milaboratory/mixcr/blob/develop/LICENSE
 */
package com.milaboratory.mixcr.vdjaligners;

import com.milaboratory.core.alignment.AlignmentScoring;
import com.milaboratory.core.sequence.NucleotideSequence;

/**
 * Define common fields for alignment parameters for clones.
 *
 * ClonalGeneAlignmentParameters don't have some properties like target gene feature like {@link
 * GeneAlignmentParameters}.
 *
 * Created by poslavsky on 01/03/2017.
 */
public interface ClonalGeneAlignmentParameters {
    /**
     * Define score threshold for hits relative to top hit score.
     *
     * @return relative min score
     */
    float getRelativeMinScore();

    /**
     * Alignment scoring
     *
     * @return alignment scoring
     */
    AlignmentScoring<NucleotideSequence> getScoring();

    /**
     * Returns a copy of this object
     *
     * @return copy of this object
     */
    ClonalGeneAlignmentParameters clone();
}
