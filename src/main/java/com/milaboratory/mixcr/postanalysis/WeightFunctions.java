package com.milaboratory.mixcr.postanalysis;

import com.milaboratory.mixcr.basictypes.Clone;

/**
 *
 */
public final class WeightFunctions {
    private WeightFunctions() {}

    public static final Count Count = new Count();

    public static final class Count implements WeightFunction<Clone> {
        @Override
        public double weight(Clone clone) {
            return clone.getCount();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 17;
        }
    }

    @SuppressWarnings("rawtypes")
    public static final NoWeight NoWeight = new NoWeight();

    public static final class NoWeight<T> implements WeightFunction<T> {
        @Override
        public double weight(T clone) {
            return 1.0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 11;
        }
    }

    public static <T> WeightFunction<T> Default() {return new DefaultWtFunction<T>();}

    public static class DefaultWtFunction<T> implements WeightFunction<T> {
        @Override
        public double weight(T o) {
            if (o instanceof Clone)
                return ((Clone) o).getCount();
            else
                throw new RuntimeException("Unsupported for class: " + o.getClass());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 12;
        }
    }
}
