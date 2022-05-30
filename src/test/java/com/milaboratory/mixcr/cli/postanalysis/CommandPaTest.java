package com.milaboratory.mixcr.cli.postanalysis;

import com.milaboratory.mixcr.cli.Main;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class CommandPaTest {
    @Test
    public void test1() {
        Main.main("postanalysis", "help", "overlap");
    }

    @Ignore
    @Test
    public void test2() {
        Main.main("postanalysis", "overlap",
//                "--chains", "IGH",
                "-f",
                "--only-productive", "-d", "umi-count-auto",
                "--metadata", "/Users/poslavskysv/Projects/milab/mixcr-test-data/metadata.tsv",
                "--chains-column", "Chain",
                "--group", "CellPopulation",
                "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_Treg_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_spl_Treg_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m10_ct_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m11_ct_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m12_ct_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m13_ct_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_Treg_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_spl_Treg_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m14_ct_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m1_young_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m2_young_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m3_young_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m4_young_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_spl_CD4naiv_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_spl_CD4naiv_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_spl_Cd8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_CD4naiv_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_CD4naiv_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_Cd8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_ct_65w_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m5_young_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_spl_CD4naiv_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_spl_CD4naiv_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_spl_Cd8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_th_Cd8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m6_ct_65w_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_spl_CD4naiv_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_spl_CD4naiv_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_spl_Cd8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_CD4m_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_dp_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m7_ct_65w_th_dp_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m8_ct_65w_th_DP_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_spl_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_spl_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_spl_CD4naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_spl_CD4naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_spl_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_spl_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_spl_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_spl_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_th_CD4mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_th_CD4mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_th_CD8mem_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_th_CD8mem_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_th_CD8naive_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_th_CD8naive_b.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_th_DP_a.sorted.clns", "/Users/poslavskysv/Projects/milab/mixcr-test-data/results/m9_ct_65w_th_DP_b.sorted.clns",
                "/Users/poslavskysv/Projects/milab/mixcr-test-data/pa/overlapPa.json");
    }
}