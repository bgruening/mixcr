package com.milaboratory.mixcr.postanalysis.downsampling;

import cc.redberry.pipe.CUtils;
import com.milaboratory.mixcr.postanalysis.Dataset;
import com.milaboratory.mixcr.postanalysis.TestDataset;
import com.milaboratory.mixcr.postanalysis.TestObject;
import com.milaboratory.mixcr.postanalysis.overlap.OverlapGroup;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.Well512a;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.milaboratory.mixcr.postanalysis.downsampling.DownsamplingPreprocessorTest.toList;

/**
 *
 */
public class OverlapDownsamplingPreprocessorTest {
    @Test
    public void test1() {
        RandomDataGenerator rng = new RandomDataGenerator(new Well512a());
        DownsampleValueChooser.Minimal chooser = new DownsampleValueChooser.Minimal();
        OverlapDownsamplingPreprocessor<TestObject> proc = new OverlapDownsamplingPreprocessor<>(
                t -> Math.round(t.weight),
                (t, newW) -> new TestObject(t.value, newW),
                chooser,
                System.currentTimeMillis()
        );

        DatasetSupport[] datasets = new DatasetSupport[]{
                rndDataset(rng, 5, 100000),
                rndDataset(rng, 10, 10000),
                rndDataset(rng, 15, 10000)
        };
        Function<Dataset<OverlapGroup<TestObject>>, Dataset<OverlapGroup<TestObject>>> downsampler = proc.setup(datasets);

        for (DatasetSupport in : datasets) {
            long dsValue = chooser.compute(in.counts);
            DatasetSupport dw = new DatasetSupport(downsampler.apply(in));

            for (int i = 0; i < in.counts.length; i++) {
                Assert.assertEquals(dsValue, dw.counts[i]);
            }

            for (OverlapGroup<TestObject> row : dw) {
                for (int i = 0; i < row.size(); i++) {
                    for (TestObject t : row.getBySample(i)) {
                        Assert.assertTrue(in.sets[i].contains(t.value));
                    }
                }
            }
        }
    }

    private static long[] sum(long[] a, long[] b) {
        if (a.length == 0)
            return b;
        if (b.length == 0)
            return a;

        long[] r = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] + b[i];
        }
        return r;
    }

    private static final class DatasetSupport extends TestDataset<OverlapGroup<TestObject>> {
        final long[] counts;
        final Set<Double>[] sets;

        public DatasetSupport(Dataset<OverlapGroup<TestObject>> data) {
            this(toList(CUtils.it(data.mkElementsPort())));
        }

        public DatasetSupport(List<OverlapGroup<TestObject>> data) {
            super(data);
            this.counts = data.stream()
                    .map(row -> row.stream().mapToLong(l -> Math.round(l.stream().mapToDouble(e -> e.weight).sum())).toArray())
                    .reduce(new long[0], OverlapDownsamplingPreprocessorTest::sum);
            //noinspection unchecked
            this.sets = new Set[counts.length];
            for (OverlapGroup<TestObject> row : data) {
                for (int i = 0; i < row.size(); i++) {
                    Set<Double> s = sets[i];
                    if (s == null)
                        s = sets[i] = new HashSet<>();
                    for (TestObject t : row.getBySample(i)) {
                        s.add(t.value);
                    }
                }
            }
        }
    }

    private static DatasetSupport rndDataset(RandomDataGenerator rng, int nSamples, int size) {
        List<OverlapGroup<TestObject>> data = new ArrayList<>();
        int p = Math.max(2, nSamples / 4);
        for (int i = 0; i < size; i++) {
            List<List<TestObject>> row = new ArrayList<>(nSamples);
            boolean added = false;
            for (int j = 0; j < nSamples; j++) {
                if (rng.nextInt(0, p) == 0) {
                    added = true;
                    ArrayList<TestObject> cell = new ArrayList<>();
                    for (int k = 0; k < rng.nextInt(1, 2); k++) {
                        cell.add(new TestObject(rng.nextUniform(0, 1), rng.nextInt(1, 1000)));
                    }
                    row.add(cell);
                } else {
                    row.add(new ArrayList<>());
                }
            }
            if (!added) {
                --i;
                continue;
            }
            data.add(new OverlapGroup<>(row));
        }
        return new DatasetSupport(data);
    }
}
