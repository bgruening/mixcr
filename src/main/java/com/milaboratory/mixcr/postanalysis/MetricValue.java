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
package com.milaboratory.mixcr.postanalysis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class MetricValue<K> {
    /** metric key (constant string / gene / etc.) */
    @JsonProperty("key")
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public final K key;
    @JsonProperty("value")
    public final double value;

    @JsonCreator
    public MetricValue(@JsonProperty("key") K key,
                       @JsonProperty("value") double value) {
        this.key = key;
        this.value = value;
    }

    public MetricValue<K> divide(double divisor) {
        return new MetricValue<>(key, value / divisor);
    }

    public MetricValue<K> withKey(K newKey) {
        return new MetricValue<>(newKey, value);
    }

    public <R> MetricValue<R> mapKey(Function<K, R> mapper) {
        return new MetricValue<>(mapper.apply(key), value);
    }

    public MetricValue<K> square() {
        return new MetricValue<K>(key, value * value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricValue<?> that = (MetricValue<?>) o;
        return Double.compare(that.value, value) == 0 && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return key + ": " + value;
    }

    private static final Object noKey = new Object();

    @SuppressWarnings("unchecked")
    public static <R> R noKey() {
        return (R) noKey;
    }
}
