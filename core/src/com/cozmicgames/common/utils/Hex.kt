package com.cozmicgames.common.utils

object Hex {
    private const val DIGITS = "0123456789ABCDEF"
    private val DIGITS_UPPER = DIGITS.uppercase()
    private val DIGITS_LOWER = DIGITS.lowercase()

    fun decodeChar(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> -1
    }

    fun encodeCharLower(v: Int): Char = DIGITS_LOWER[v]

    fun encodeCharUpper(v: Int): Char = DIGITS_UPPER[v]

    fun isHexDigit(c: Char): Boolean = decodeChar(c) >= 0

    fun decode(str: String): ByteArray {
        val out = ByteArray((str.length + 1) / 2)
        var opos = 0
        var nibbles = 0
        var value = 0
        for (element in str) {
            val vv = decodeChar(element)
            if (vv >= 0) {
                value = (value shl 4) or vv
                nibbles++
            }
            if (nibbles == 2) {
                out[opos++] = value.toByte()
                nibbles = 0
                value = 0
            }
        }

        return if (opos != out.size) out.copyOf(opos) else out
    }

    fun encodeLower(src: ByteArray): String = encodeBase(src, DIGITS_LOWER)

    fun encodeUpper(src: ByteArray): String = encodeBase(src, DIGITS_UPPER)

    private fun encodeBase(data: ByteArray, digits: String = DIGITS): String {
        val out = StringBuilder(data.size * 2)
        for (v in data) {
            out.append(digits[((v.toInt() and 0xFF) ushr 4) and 0xF])
            out.append(digits[((v.toInt() and 0xFF) ushr 0) and 0xF])
        }
        return out.toString()
    }
}