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
package com.milaboratory.mixcr.assembler.preclone;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mixcr.basictypes.GeneAndScore;
import com.milaboratory.mixcr.basictypes.tag.TagCount;
import com.milaboratory.mixcr.basictypes.tag.TagTuple;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.Serializer;
import io.repseq.core.ExtendedReferencePoints;
import io.repseq.core.GeneType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class IO {
    private IO() {
    }

    public static final class PreCloneImplSerializer implements Serializer<PreCloneImpl> {
        @Override
        public void write(PrimitivO output, PreCloneImpl obj) {
            output.writeLong(obj.index);
            output.writeObject(obj.coreKey);
            output.writeObject(obj.clonalSequence);
            output.writeObject(obj.coreTagCount);
            output.writeObject(obj.fullTagCount);
            output.writeInt(obj.geneScores.size());
            for (Map.Entry<GeneType, List<GeneAndScore>> e : obj.geneScores.entrySet()) {
                output.writeObject(e.getKey());
                output.writeInt(e.getValue().size());
                for (GeneAndScore gs : e.getValue())
                    output.writeObject(gs);
            }
            output.writeObject(obj.referencePoints);
            output.writeInt(obj.numberOfReads);
        }

        @Override
        public PreCloneImpl read(PrimitivI input) {
            long index = input.readLong();
            TagTuple coreKey = input.readObject(TagTuple.class);
            NSequenceWithQuality[] clonalSequence = input.readObject(NSequenceWithQuality[].class);
            TagCount coreTagCount = input.readObject(TagCount.class);
            TagCount fullTagCount = input.readObject(TagCount.class);
            int count0 = input.readInt();
            EnumMap<GeneType, List<GeneAndScore>> gsss = new EnumMap<>(GeneType.class);
            for (int i0 = 0; i0 < count0; i0++) {
                GeneType gt = input.readObject(GeneType.class);
                int count1 = input.readInt();
                List<GeneAndScore> gss = new ArrayList<>();
                for (int i1 = 0; i1 < count1; i1++)
                    gss.add(input.readObject(GeneAndScore.class));
                gsss.put(gt, gss);
            }
            ExtendedReferencePoints[] referencePoints = input.readObject(ExtendedReferencePoints[].class);
            int numberOfReads = input.readInt();
            return new PreCloneImpl(index, coreKey, coreTagCount, fullTagCount, clonalSequence, gsss, referencePoints, numberOfReads);
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
