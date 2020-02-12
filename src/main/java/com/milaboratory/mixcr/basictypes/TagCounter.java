package com.milaboratory.mixcr.basictypes;

import com.milaboratory.primitivio.annotations.Serializable;
import gnu.trove.TDoubleCollection;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Serializable(by = IO.TagCounterSerializer.class)
public final class TagCounter {
    public static final TagCounter EMPTY = new TagCounter(new TObjectDoubleHashMap<>());

    final TObjectDoubleHashMap<TagTuple> tags;

    TagCounter(TObjectDoubleHashMap<TagTuple> tags) {
        this.tags = tags;
    }

    public TagCounter(TagTuple tags, double count) {
        this.tags = new TObjectDoubleHashMap<>();
        this.tags.put(tags, count);
    }

    public TagCounter(TagTuple tags) {
        this(tags, 1.0);
    }

    public double getOrDefault(TagTuple tt, double d) {
        if (!tags.containsKey(tt))
            return d;
        else
            return tags.get(tt);
    }

    public int size() {
        return tags.size();
    }

    public double get(TagTuple tt) {
        return getOrDefault(tt, Double.NaN);
    }

    public TObjectDoubleIterator<TagTuple> iterator() {
        TObjectDoubleIterator<TagTuple> it = tags.iterator();
        return new TObjectDoubleIterator<TagTuple>() {
            @Override
            public TagTuple key() {
                return it.key();
            }

            @Override
            public double value() {
                return it.value();
            }

            @Override
            public double setValue(double val) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void advance() {
                it.advance();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    @Override
    public String toString() {
        return tags.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagCounter that = (TagCounter) o;
        return tags.equals(that.tags);
    }

    @Override
    public int hashCode() {
        return tags.hashCode();
    }

    public TagCounter[] splitBy(int index) {
        Map<String, TagCounterBuilder> map = new HashMap<>();
        TObjectDoubleIterator<TagTuple> it = iterator();
        while (it.hasNext()) {
            it.advance();

            TagTuple t = it.key();
            double count = it.value();

            TagCounterBuilder tb = map.computeIfAbsent(t.tags[index], __ -> new TagCounterBuilder());
            tb.add(t, count);
        }
        return map.values().stream().map(TagCounterBuilder::createAndDestroy).toArray(TagCounter[]::new);
    }

    public double sum() {
        TDoubleCollection c = tags.valueCollection();
        TDoubleIterator it = c.iterator();
        double sum = 0;
        while (it.hasNext())
            sum += it.next();
        return sum;
    }

    public TagCounter toFractions() {
        double sum = sum();
        if (sum == 0)
            return this;
        TObjectDoubleHashMap<TagTuple> result = new TObjectDoubleHashMap<>();
        TObjectDoubleIterator<TagTuple> it = iterator();
        while (it.hasNext()) {
            it.advance();
            result.put(it.key(), it.value() / sum);
        }
        return new TagCounter(result);
    }

    public Set<String> tags(int index) {
        Set<String> set = new HashSet<>();
        TObjectDoubleIterator<TagTuple> it = iterator();
        while (it.hasNext()) {
            it.advance();
            set.add(it.key().tags[index]);
        }
        return set;
    }
}
