/*
 * Copyright (c) 2014-2020, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact MiLaboratory LLC, which owns exclusive
 * rights for distribution of this program for commercial purposes, using the
 * following email address: licensing@milaboratory.com.
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.mixcr.postanalysis.downsampling;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.mixcr.postanalysis.Dataset;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.ToLongFunction;
import java.util.stream.LongStream;

public final class DownsamplingUtil {
    private DownsamplingUtil() {
    }

    @SuppressWarnings("rawtypes")
    public static final OutputPortCloseable emptyOP = new OutputPortCloseable() {
        @Override
        public void close() { }

        @Override
        public Object take() {
            return null;
        }
    };

    @SuppressWarnings("rawtypes")
    public static final Dataset EMPTY_DATASET_SUPPLIER = Dataset.fromSupplier(() -> emptyOP);

    public static <T> long total(ToLongFunction<T> getCount, Dataset<T> set) {
        long total = 0;
        try (OutputPortCloseable<T> port = set.mkElementsPort()) {
            for (T t : CUtils.it(port))
                total += getCount.applyAsLong(t);
            return total;
        }
    }

    public static long[] downsample_mvhg(long[] counts, long downSampleSize, RandomGenerator rnd) {
        long total = LongStream.of(counts).sum();
        long[] result = new long[counts.length];
        RandomMvhgMarginals.random_multivariate_hypergeometric_marginals(rnd, total, counts, downSampleSize, result);
        return result;
    }

    public static long[] downsample_counts(long[] counts, long downSampleSize, RandomGenerator rnd) {
        long total = LongStream.of(counts).sum();
        long[] result = new long[counts.length];
        RandomMvhgCounts.random_multivariate_hypergeometric_count(rnd, total, counts, downSampleSize, result);
        return result;
    }
}