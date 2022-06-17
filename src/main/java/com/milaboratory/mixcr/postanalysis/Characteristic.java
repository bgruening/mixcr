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

import com.fasterxml.jackson.annotation.*;
import com.milaboratory.mixcr.postanalysis.additive.AdditiveCharacteristic;
import com.milaboratory.mixcr.postanalysis.diversity.DiversityCharacteristic;
import com.milaboratory.mixcr.postanalysis.overlap.OverlapCharacteristic;
import com.milaboratory.mixcr.postanalysis.spectratype.SpectratypeCharacteristic;

import java.util.Objects;

/**
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdditiveCharacteristic.class, name = "individual"),
        @JsonSubTypes.Type(value = DiversityCharacteristic.class, name = "diversity"),
        @JsonSubTypes.Type(value = OverlapCharacteristic.class, name = "overlap"),
        @JsonSubTypes.Type(value = SpectratypeCharacteristic.class, name = "geneFeatureSpectratype"),
        @JsonSubTypes.Type(value = Characteristic.CharacteristicWrapper.class, name = "characteristicWrapper")
})
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class Characteristic<K, T> {
    /** Unique characteristic name */
    @JsonProperty("name")
    public final String name;
    @JsonProperty("preproc")
    public final SetPreprocessorFactory<T> preprocessor;
    @JsonProperty("weight")
    public final WeightFunction<T> weight;

    public Characteristic(String name,
                          SetPreprocessorFactory<T> preprocessor,
                          WeightFunction<T> weight) {
        this.name = name;
        this.preprocessor = preprocessor;
        this.weight = weight;
    }

    /** Create aggregator for further processing of a given dataset */
    protected abstract Aggregator<K, T> createAggregator(Dataset<T> dataset);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Characteristic<?, ?> that = (Characteristic<?, ?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(preprocessor, that.preprocessor) &&
                Objects.equals(weight, that.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, preprocessor, weight);
    }

    /** override name & preproc */
    public Characteristic<K, T> override(String nameOverride, SetPreprocessorFactory<T> preprocOverride) {
        return new CharacteristicWrapper<>(nameOverride, preprocOverride, this);
    }

    public static final class CharacteristicWrapper<K, T> extends Characteristic<K, T> {
        @JsonProperty("inner")
        public final Characteristic<K, T> inner;

        @JsonCreator
        public CharacteristicWrapper(@JsonProperty("name") String name,
                                     @JsonProperty("preproc") SetPreprocessorFactory<T> preprocessor,
                                     @JsonProperty("inner") Characteristic<K, T> inner) {
            super(name == null ? inner.name : name, preprocessor == null ? inner.preprocessor : preprocessor, inner.weight);
            this.inner = inner;
        }

        @Override
        public Characteristic<K, T> override(String nameOverride, SetPreprocessorFactory<T> preprocOverride) {
            return new CharacteristicWrapper<>(nameOverride, preprocOverride, inner);
        }

        @Override
        protected Aggregator<K, T> createAggregator(Dataset<T> dataset) {
            return inner.createAggregator(dataset);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            CharacteristicWrapper<?, ?> that = (CharacteristicWrapper<?, ?>) o;
            return Objects.equals(inner, that.inner);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), inner);
        }
    }
}
