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
package com.milaboratory.mixcr.postanalysis.overlap;

import java.util.Arrays;

/**
 *
 */
public enum OverlapType {
    D("Unweighted overlap", "Relative overlap diversity normalized"),
    SharedClonotypes("Clonotypes", "Number of shared clonotypes"),
    F1("Frequencies", "Geometric mean of relative overlap frequencies"),
    F2("Weighted overlap", "Сlonotype-wise sum of geometric mean frequencies"),
    Jaccard("Jaccard", "Jaccard overlap"),
    R_Intersection("Pearson", "Pearson correlation of clonotype frequencies, restricted only to the overlapping clonotypes"),
    R_All("Pearson (all)", "Pearson correlation of clonotype frequencies (outer merge)");

    public final String name;
    public final String description;

    OverlapType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static OverlapType byName(String name) {
        return Arrays.stream(values()).filter(s -> s.name.equals(name)).findFirst().orElse(null);
    }
}
