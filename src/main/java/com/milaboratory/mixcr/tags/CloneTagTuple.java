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
package com.milaboratory.mixcr.tags;

import com.milaboratory.mixcr.basictypes.Clone;

import java.util.Objects;

/**
 *
 */
public final class CloneTagTuple {
    final Clone clone;
    final String tag;
    int rank;
    long umiCount;
    double readCount, readFractionInTag, umiFractionInTag;

    public CloneTagTuple(Clone clone, String tag) {
        this.clone = clone;
        this.tag = tag;
    }

    public double getReadsInTag() {
        return readCount / readFractionInTag;
    }

    public double getUMIsInTag() {
        return umiCount / umiFractionInTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloneTagTuple that = (CloneTagTuple) o;
        return rank == that.rank &&
                umiCount == that.umiCount &&
                Double.compare(that.readCount, readCount) == 0 &&
                Double.compare(that.readFractionInTag, readFractionInTag) == 0 &&
                Double.compare(that.umiFractionInTag, umiFractionInTag) == 0 &&
                Objects.equals(clone, that.clone) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clone, tag, rank, umiCount, readCount, readFractionInTag, umiFractionInTag);
    }
}
