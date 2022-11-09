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
package com.milaboratory.mixcr.basictypes.tag;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.Serializer;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.Objects;

public final class IO {
    private IO() {
    }

    public static class SequenceAndQualityTagValueSerializer implements Serializer<SequenceAndQualityTagValue> {
        @Override
        public void write(PrimitivO output, SequenceAndQualityTagValue obj) {
            output.writeObject(obj.data);
        }

        @Override
        public SequenceAndQualityTagValue read(PrimitivI input) {
            return new SequenceAndQualityTagValue(input.readObject(NSequenceWithQuality.class));
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public boolean handlesReference() {
            return false;
        }
    }

    public static class SequenceTagValueSerializer implements Serializer<SequenceTagValue> {
        @Override
        public void write(PrimitivO output, SequenceTagValue obj) {
            output.writeObject(obj.value);
        }

        @Override
        public SequenceTagValue read(PrimitivI input) {
            return new SequenceTagValue(input.readObject(NucleotideSequence.class));
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public boolean handlesReference() {
            return false;
        }
    }

    public static class StringTagValueSerializer implements Serializer<StringTagValue> {
        @Override
        public void write(PrimitivO output, StringTagValue obj) {
            output.writeUTF(obj.value);
        }

        @Override
        public StringTagValue read(PrimitivI input) {
            return new StringTagValue(input.readUTF());
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public boolean handlesReference() {
            return false;
        }
    }

    public static class LongTagValueSerializer implements Serializer<LongTagValue> {
        @Override
        public void write(PrimitivO output, LongTagValue obj) {
            output.writeLong(obj.value);
        }

        @Override
        public LongTagValue read(PrimitivI input) {
            return new LongTagValue(input.readLong());
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public boolean handlesReference() {
            return false;
        }
    }

    public static class TagTupleSerializer implements Serializer<TagTuple> {
        @Override
        public void write(PrimitivO output, TagTuple obj) {
            output.writeObject(obj.tags);
        }

        @Override
        public TagTuple read(PrimitivI input) {
            //noinspection ConstantConditions
            return new TagTuple(input.readObject(TagValue[].class));
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public boolean handlesReference() {
            return false;
        }
    }

    public static class TagCounterSerializer implements Serializer<TagCount> {
        @Override
        public void write(PrimitivO output, TagCount object) {
            output.writeInt(object.size());
            TObjectDoubleIterator<TagTuple> it = object.iterator();
            while (it.hasNext()) {
                it.advance();
                output.writeObject(it.key().tags);
                output.writeDouble(it.value());
            }
        }

        @Override
        public TagCount read(PrimitivI input) {
            int len = input.readInt();
            if (len == 0)
                throw new IllegalArgumentException();
            else if (len == 1) {
                TagValue[] tags = input.readObject(TagValue[].class);
                Objects.requireNonNull(tags);
                double count = input.readDouble();
                return new TagCount(tags.length == 0 ? TagTuple.NO_TAGS : new TagTuple(tags), count);
            } else {
                TObjectDoubleHashMap<TagTuple> r = new TObjectDoubleHashMap<>(len, Constants.DEFAULT_LOAD_FACTOR, 0.0);
                for (int i = 0; i < len; ++i) {
                    TagValue[] tags = input.readObject(TagValue[].class);
                    Objects.requireNonNull(tags);
                    double count = input.readDouble();
                    r.put(new TagTuple(tags), count);
                }
                return new TagCount(r);
            }
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public boolean handlesReference() {
            return false;
        }
    }
}
