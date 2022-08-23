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
package com.milaboratory.primitivio

import cc.redberry.pipe.CUtils
import cc.redberry.pipe.OutputPort
import cc.redberry.pipe.OutputPortCloseable
import cc.redberry.pipe.blocks.Buffer
import cc.redberry.pipe.blocks.FilteringPort
import cc.redberry.pipe.util.FlatteningOutputPort
import cc.redberry.pipe.util.TBranchOutputPort
import com.milaboratory.primitivio.blocks.PrimitivIBlocks
import com.milaboratory.primitivio.blocks.PrimitivIOBlocksUtil
import com.milaboratory.primitivio.blocks.PrimitivOBlocks
import com.milaboratory.util.TempFileDest
import net.jpountz.lz4.LZ4Compressor
import java.util.concurrent.ThreadLocalRandom

fun <T : Any> PrimitivO.writeObjectOptional(obj: T?) {
    if (obj == null) {
        writeBoolean(false)
    } else {
        writeBoolean(true)
        writeObject(obj)
    }
}

inline fun <reified T : Any> PrimitivI.readObjectOptional(): T? {
    val exist = readBoolean()
    return if (exist) {
        readObject(T::class.java)
    } else {
        null
    }
}

inline fun <reified T : Any> PrimitivI.readObjectRequired(): T {
    val result = readObject(T::class.java)
    if (result != null) {
        return result
    } else {
        throw IllegalStateException("Error on read ${T::class}, expected not null, but was null")
    }
}

inline fun <reified K : Any, reified V : Any> PrimitivI.readMap(): Map<K, V> =
    Util.readMap(this, K::class.java, V::class.java)

inline fun <reified T : Any> PrimitivI.readList(): List<T> = readCollection { mutableListOf() }

inline fun <reified T : Any> PrimitivI.readSet(): Set<T> = readCollection { mutableSetOf() }

fun PrimitivO.writeCollection(collection: Collection<*>) {
    this.writeInt(collection.size)
    collection.forEach {
        this.writeObject(it)
    }
}

inline fun <reified T : Any, C : MutableCollection<T>> PrimitivI.readCollection(supplier: (size: Int) -> C): C {
    var size = this.readInt()
    val list = supplier(size)
    while (--size >= 0) list.add(this.readObjectRequired())
    return list
}

fun PrimitivO.writeMap(array: Map<*, *>) = Util.writeMap(array, this)

fun <T : Any> PrimitivO.writeArray(array: Array<T>) {
    this.writeInt(array.size)
    for (o in array) this.writeObject(o)
}

inline fun <reified T : Any> PrimitivI.readArray(): Array<T> = Array(readInt()) {
    readObject(T::class.java)
}

fun <T : Any, R : Any> OutputPort<T>.map(function: (T) -> R): OutputPort<R> = CUtils.wrap(this, function)

fun <T : Any, R : Any> OutputPort<T>.mapInParallel(
    threads: Int,
    buffer: Int = Buffer.DEFAULT_SIZE,
    function: (T) -> R
): OutputPort<R> = CUtils.orderedParallelProcessor(this, function, buffer, threads)

fun <T : Any, R : Any> OutputPort<T>.mapNotNull(function: (T) -> R?): OutputPortCloseable<R> = flatMap {
    listOfNotNull(function(it))
}

fun <T : Any> List<OutputPort<T>>.flatten(): OutputPortCloseable<T> =
    FlatteningOutputPort(CUtils.asOutputPort(this))

fun <T : Any> OutputPort<List<T>>.flatten(): OutputPortCloseable<T> = flatMap { it }

fun <T : Any, R : Any> OutputPort<T>.flatMap(function: (element: T) -> Iterable<R>): OutputPortCloseable<R> =
    FlatteningOutputPort(CUtils.wrap(this) {
        CUtils.asOutputPort(function(it))
    })

fun <T : Any> OutputPort<T>.filter(test: (element: T) -> Boolean): OutputPortCloseable<T> =
    FilteringPort(this, test)

fun <T : Any> OutputPort<T>.forEach(action: (element: T) -> Unit): Unit =
    CUtils.it(this).forEach(action)

fun <T : Any> OutputPort<T>.forEachInParallel(threads: Int, action: (element: T) -> Unit): Unit =
    CUtils.processAllInParallel(this, action, threads)

fun <T : Any> OutputPort<T>.toList(): List<T> =
    CUtils.it(this).toList()

fun <T : Any> OutputPort<T>.asSequence(): Sequence<T> =
    CUtils.it(this).asSequence()

fun <T : Any> OutputPort<T>.count(): Int =
    CUtils.it(this).count()

inline fun <reified T : Any, R> OutputPort<T>.cached(
    tempDest: TempFileDest,
    stateBuilder: PrimitivIOStateBuilder,
    blockSize: Int,
    concurrencyToRead: Int = 1,
    concurrencyToWrite: Int = 1,
    compressor: LZ4Compressor = PrimitivIOBlocksUtil.fastLZ4Compressor(),
    function: (() -> OutputPort<T>) -> R
): R {
    val primitivI = PrimitivIBlocks(T::class.java, concurrencyToRead, stateBuilder.iState)
    val primitivO = PrimitivOBlocks<T>(
        concurrencyToWrite,
        stateBuilder.oState,
        blockSize,
        compressor
    )

    val tempFile = tempDest.resolvePath("tempFile." + ThreadLocalRandom.current().nextInt())

    var wasCalled = false
    val portForFirstCall = TBranchOutputPort.wrap(primitivO.newWriter(tempFile), this)
    val result = function {
        if (!wasCalled) {
            wasCalled = true
            portForFirstCall
        } else {
            check(portForFirstCall.isClosed) {
                "First result from cache must be read entirely before calling the next time"
            }
            primitivI.newReader(tempFile, blockSize)
        }
    }
    tempFile.toFile().delete()
    (this as? OutputPortCloseable)?.close()
    return result
}
