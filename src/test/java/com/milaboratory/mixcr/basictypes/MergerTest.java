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
package com.milaboratory.mixcr.basictypes;

import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Aligner;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class MergerTest {
    //@Test
    //public void test1() throws Exception {
    //    NucleotideSequence sequence = new NucleotideSequence("AGTCTAGTCGTAGACGCGCTGATTAGCGTAGGTCGGTCGTATT");
    //    NSequenceWithQuality lTarget = new NSequenceWithQuality(
    //            "attgcAGTCTAGTCGTAGCGCGACGATTAGCGT",
    //            "JJJJJJJJJJJJJJJJJHJJJJBJJJJJJJJJJ");
    //    NSequenceWithQuality rTarget = new NSequenceWithQuality(
    //            "TAGACGCGTTCCGATTAGCGTAGGTCGGTCGTATTaggta",
    //            "JJJJJJJJHJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ");
    //    Alignment<NucleotideSequence> lAlignment = Aligner.alignLocalLinear(new LinearGapAlignmentScoring<>(NucleotideSequence.ALPHABET, 4, -5, -9), sequence, lTarget.getSequence());
    //    Alignment<NucleotideSequence> rAlignment = Aligner.alignLocalLinear(new LinearGapAlignmentScoring<>(NucleotideSequence.ALPHABET, 4, -5, -9), sequence, rTarget.getSequence());
    //
    //    System.out.println(lAlignment.getAlignmentHelper().toStringWithSeq2Quality(lTarget.getQuality()));
    //    System.out.println();
    //    System.out.println(rAlignment.getAlignmentHelper().toStringWithSeq2Quality(rTarget.getQuality()));
    //    System.out.println();
    //    System.out.println(Merger.merge(new Range(0, sequence.size()), new Alignment[]{lAlignment, rAlignment}, new NSequenceWithQuality[]{lTarget, rTarget}).toPrettyString());
    //}

    @Test
    public void test2() throws Exception {
        mAssert("CGCACAGTGTTGTCAAAGAAAACGCGTACGACATTGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "CGCACAGTGTTGTCAAAGAAAACGCGTACGACATTGAGAAGACCGGCC",
                "CGACATTGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "CGCACAGTGTTGTCAAAGAAAACGCGTACGACATTGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAANNNNNNNNNNNNNNNNNNNNBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        mAssert("CGCACAGTGTTGTCAAAGAAAACGCGTACGACATTGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "CGCACAGTGTTGTCAAAGAAAACGCGTACGACATTGAGAAGACCGGCC",
                /*                        */"CGACATCGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "CGCACAGTGTTGTCAAAGAAAACGCGTACGACATCGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAANNNNNNBNNNNNNNNNNNNNBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        mAssert("CGCACAGTGTTGTCAAAGAAAACGCGTACGACATTGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "CGCACAGTGTTGTCAAAGAAAACGCGTACGACATCGAGAAGACCGGCC",
                /*                        */"CGACATTGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "CGCACAGTGTTGTCAAAGAAAACGCGTACGACATTGAGAAGACCGGCCGTTCTCCTTTGACATGATTGGATCGGTTGCTGCCGGCCCAGAATCCTAGCAG",
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAANNNNNNBNNNNNNNNNNNNNBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
    }

    public static void mAssert(String originalSeq,
                               String seq1, String seq2,
                               String expectedSequence, String expectedQuality) {
        NucleotideSequence originalSequence = new NucleotideSequence(originalSeq);
        NSequenceWithQuality target1 = new NSequenceWithQuality(seq1, lets('A', seq1.length()));
        NSequenceWithQuality target2 = new NSequenceWithQuality(seq2, lets('B', seq2.length()));
        LinearGapAlignmentScoring<NucleotideSequence> scoring = new LinearGapAlignmentScoring<>(NucleotideSequence.ALPHABET, 4, -5, -9);
        Alignment<NucleotideSequence> alignment1 = Aligner.alignLocalLinear(scoring, originalSequence, target1.getSequence());
        Alignment<NucleotideSequence> alignment2 = Aligner.alignLocalLinear(scoring, originalSequence, target2.getSequence());
        NSequenceWithQuality result = Merger.merge(new Range(0, originalSequence.size()), new Alignment[]{alignment1, alignment2}, new NSequenceWithQuality[]{target1, target2});
        Assert.assertEquals(expectedSequence, result.getSequence().toString());
        Assert.assertEquals(expectedQuality, result.getQuality().toString());
    }

    public static String lets(char letter, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, letter);
        return new String(chars);
    }
}