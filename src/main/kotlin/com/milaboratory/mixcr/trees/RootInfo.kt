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
package com.milaboratory.mixcr.trees

import com.milaboratory.core.Range
import com.milaboratory.core.sequence.NucleotideSequence
import com.milaboratory.primitivio.PrimitivI
import com.milaboratory.primitivio.PrimitivO
import com.milaboratory.primitivio.Serializer
import com.milaboratory.primitivio.annotations.Serializable
import com.milaboratory.primitivio.readObjectRequired
import io.repseq.core.GeneType
import io.repseq.core.GeneType.Joining
import io.repseq.core.GeneType.Variable

@Serializable(by = RootInfoSerializer::class)
data class RootInfo(
    val VSequence: NucleotideSequence,
    val VRangeInCDR3: Range,
    val reconstructedNDN: NucleotideSequence,
    val JSequence: NucleotideSequence,
    val JRangeInCDR3: Range,
    val VJBase: VJBase
) {
    fun baseCDR3(): NucleotideSequence = VSequence.getRange(VRangeInCDR3)
        .concatenate(reconstructedNDN)
        .concatenate(JSequence.getRange(JRangeInCDR3))

    fun getSequence1(geneType: GeneType) = when (geneType) {
        Joining -> JSequence
        Variable -> VSequence
        else -> throw IllegalArgumentException()
    }
}

class RootInfoSerializer : Serializer<RootInfo> {
    override fun write(output: PrimitivO, obj: RootInfo) {
        output.writeObject(obj.VSequence)
        output.writeObject(obj.VRangeInCDR3)
        output.writeObject(obj.reconstructedNDN)
        output.writeObject(obj.JSequence)
        output.writeObject(obj.JRangeInCDR3)
        output.writeObject(obj.VJBase)
    }

    override fun read(input: PrimitivI): RootInfo = RootInfo(
        input.readObjectRequired(),
        input.readObjectRequired(),
        input.readObjectRequired(),
        input.readObjectRequired(),
        input.readObjectRequired(),
        input.readObjectRequired()
    )

    override fun isReference(): Boolean = false

    override fun handlesReference(): Boolean = false
}