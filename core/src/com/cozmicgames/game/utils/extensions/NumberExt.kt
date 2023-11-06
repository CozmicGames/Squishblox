package com.cozmicgames.game.utils.extensions

import com.badlogic.gdx.Gdx
import java.lang.Math.toDegrees
import java.lang.Math.toRadians

fun Int.toStringUnsigned(radix: Int): String = toUInt().toString(radix)

fun Long.toStringUnsigned(radix: Int): String = toULong().toString(radix)

fun UInt.rotateLeft(bits: Int): UInt = (this shl bits) or (this shr (32 - bits))

fun UInt.rotateRight(bits: Int): UInt = (this shl (32 - bits)) or (this shr bits)

fun Int.rotateLeft(bits: Int): Int = (this shl bits) or (this ushr (32 - bits))

fun Int.rotateRight(bits: Int): Int = (this shl (32 - bits)) or (this ushr bits)

fun Long.rotateLeft(bits: Int): Long = (this shl bits) or (this ushr (64 - bits))

fun Long.rotateRight(bits: Int): Long = (this shl (64 - bits)) or (this ushr bits)

fun Short.reverseBytes(): Short {
    val low = ((this.toInt() ushr 0) and 0xFF)
    val high = ((this.toInt() ushr 8) and 0xFF)
    return ((high and 0xFF) or (low shl 8)).toShort()
}

fun Int.reverseBytes(): Int {
    val v0 = ((this ushr 0) and 0xFF)
    val v1 = ((this ushr 8) and 0xFF)
    val v2 = ((this ushr 16) and 0xFF)
    val v3 = ((this ushr 24) and 0xFF)
    return (v0 shl 24) or (v1 shl 16) or (v2 shl 8) or (v3 shl 0)
}

fun Long.reverseBytes(): Long {
    val v0 = (this ushr 0).toInt().reverseBytes().toLong() and 0xFFFFFFFFL
    val v1 = (this ushr 32).toInt().reverseBytes().toLong() and 0xFFFFFFFFL
    return (v0 shl 32) or (v1 shl 0)
}

fun Char.reverseBytes(): Char = this.code.toShort().reverseBytes().toInt().toChar()

fun Int.signExtend(bits: Int): Int = (this shl (32 - bits)) shr (Int.SIZE_BITS - bits)

fun Long.signExtend(bits: Int): Long = (this shl (64 - bits)) shr (Long.SIZE_BITS - bits)

fun Int.mask(): Int = (1 shl this) - 1

fun Long.mask(): Long = (1L shl this.toInt()) - 1L

fun Int.extract(offset: Int, count: Int): Int = (this ushr offset) and count.mask()

fun Int.extract(offset: Int): Boolean = ((this ushr offset) and 1) != 0

fun Int.extractScaled(offset: Int, count: Int, scale: Int): Int {
    val mask = count.mask()
    return (extract(offset, count) * scale) / mask
}

fun Int.insert(value: Int, offset: Int, count: Int): Int {
    val mask = count.mask()
    val clearValue = this and (mask shl offset).inv()
    return clearValue or ((value and mask) shl offset)
}

fun Int.insert(value: Boolean, offset: Int): Int = this.insert(if (value) 1 else 0, offset, 1)


fun Int.insertScaled(value: Int, offset: Int, count: Int, scale: Int): Int {
    val mask = count.mask()
    return insert((value * mask) / scale, offset, count)
}

val Int.isOdd get() = (this % 2) == 1

val Int.isEven get() = (this % 2) == 0

val Int.nextPowerOfTwo: Int
    get() {
        var v = this
        v--
        v = v or (v shr 1)
        v = v or (v shr 2)
        v = v or (v shr 4)
        v = v or (v shr 8)
        v = v or (v shr 16)
        v++
        return v
    }

val Int.prevPowerOfTwo: Int get() = if (isPowerOfTwo) this else (nextPowerOfTwo ushr 1)

val Int.isPowerOfTwo: Boolean get() = this != 0 && (this and (this - 1)) == 0

fun <T : Comparable<T>> T.clamp(min: T, max: T): T = if (this <= min) min else if (this >= max) max else this

fun Int.mix(color: Int, amount: Float): Int {
    val oneMinusAmt = 1.0f - amount
    val r = ((((this shr 24) and 0xFF) * amount) + (((color shr 24) and 0xFF) * oneMinusAmt)).toInt()
    val g = ((((this shr 16) and 0xFF) * amount) + (((color shr 16) and 0xFF) * oneMinusAmt)).toInt()
    val b = ((((this shr 8) and 0xFF) * amount) + (((color shr 8) and 0xFF) * oneMinusAmt)).toInt()
    val a = (((this and 0xFF) * amount) + ((color and 0xFF) * oneMinusAmt)).toInt()
    return (r shl 24) or (g shl 16) or (b shl 8) or a
}

fun Int.tint(color: Int): Int {
    val r = (((this shr 24) and 0xFF) * ((color shr 24) and 0xFF)) shr 8
    val g = (((this shr 16) and 0xFF) * ((color shr 16) and 0xFF)) shr 8
    val b = (((this shr 8) and 0xFF) * ((color shr 8) and 0xFF)) shr 8
    val a = ((this and 0xFF) * (color and 0xFF)) shr 8
    return (r shl 24) or (g shl 16) or (b shl 8) or a
}

fun Int.rgbaToAbgr(): Int {
    val r = (this shr 24) and 0xFF
    val g = (this shr 16) and 0xFF
    val b = (this shr 8) and 0xFF
    val a = this and 0xFF
    return (a shl 24) or (b shl 16) or (g shl 8) or r
}

val Char.isLetter get() = (this >= 'a' && this <= 'z') || (this >= 'A' && this <= 'Z')

val Char.isDigit get() = this >= '0' && this <= '9'

val Int.numberOfTrailingZeros: Int
    get() {
        var i = this

        if (i == 0)
            return 32

        var n = 31

        var y = i shl 16

        if (y != 0) {
            n -= 16
            i = y
        }

        y = i shl 8

        if (y != 0) {
            n -= 8
            i = y
        }

        y = i shl 4

        if (y != 0) {
            n -= 4
            i = y
        }

        y = i shl 2

        if (y != 0) {
            n -= 2
            i = y
        }

        return n - (i shl 1 ushr 31)
    }

val Long.numberOfTrailingZeros: Int
    get() {
        var x: Long
        if (this == 0L)
            return 64

        var n = 63

        var y = this
        if (y != 0L) {
            n -= 32
            x = y
        } else
            x = (this ushr 32)

        y = x shl 16
        if (y != 0L) {
            n -= 16
            x = y
        }

        y = x shl 8
        if (y != 0L) {
            n -= 8
            x = y
        }
        y = x shl 4
        if (y != 0L) {
            n -= 4
            x = y
        }
        y = x shl 2
        if (y != 0L) {
            n -= 2
            x = y
        }
        return (n - (x shl 1 ushr 31)).toInt()
    }

fun pack2x32(a: Int, b: Int) = a.toLong() and 0xFFFFFFFF or ((b.toLong() and 0xFFFFFFFF) shl 32)

fun <R> split2x32(v: Long, block: (Int, Int) -> R): R {
    val a = (v and 0xFFFFFFFF).toInt()
    val b = ((v and (0xFFFFFFFF shl 32)) shr 32).toInt()
    return block(a, b)
}

infix fun Int.alignedTo(alignment: Int) = (this + alignment - 1) - this % alignment

fun average(vararg values: Float) = values.average().toFloat()

fun average(vararg values: Double) = values.average()

fun average(vararg values: Int) = values.average().toInt()

fun average(vararg values: Short) = values.average().toInt().toShort()

fun average(vararg values: Byte) = values.average().toInt().toByte()

val Float.degrees get() = toDegrees(toDouble()).toFloat()

val Float.radians get() = toRadians(toDouble()).toFloat()

val Float.pixels get() = this

val Float.cmx get() = this * Gdx.graphics.backBufferScale * Gdx.graphics.ppcX

val Float.cmy get() = this * Gdx.graphics.backBufferScale * Gdx.graphics.ppcY
