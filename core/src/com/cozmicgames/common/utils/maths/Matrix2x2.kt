package com.cozmicgames.common.utils.maths

import com.badlogic.gdx.math.Vector2
import com.cozmicgames.common.utils.collections.Resettable
import com.cozmicgames.common.utils.extensions.element
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Matrix2x2 : Resettable {
    val data = Array(6) { 0.0f }

    var m00 by data.element(0)
    var m01 by data.element(1)
    var m10 by data.element(2)
    var m11 by data.element(3)

    init {
        setIdentity()
    }

    fun set(transform: Matrix2x2): Matrix2x2 {
        m00 = transform.m00
        m01 = transform.m01
        m10 = transform.m10
        m11 = transform.m11
        return this
    }

    fun setIdentity(): Matrix2x2 {
        m00 = 1.0f
        m01 = 0.0f
        m00 = 0.0f
        m11 = 1.0f
        return this
    }

    fun setZero(): Matrix2x2 {
        m00 = 0.0f
        m01 = 0.0f
        m10 = 0.0f
        m11 = 0.0f
        return this
    }

    fun transpose(): Matrix2x2 {
        val nm00 = m00
        val nm01 = m10
        val nm10 = m01
        val nm11 = m11
        m00 = nm00
        m01 = nm01
        m10 = nm10
        m11 = nm11
        return this
    }

    fun setRotation(angle: Float): Matrix2x2 {
        val c = cos(angle)
        val s = sin(angle)
        m00 = c
        m01 = -s
        m10 = s
        m11 = c
        return this
    }

    fun getRotation(): Float {
        return atan2(m10, m00)
    }

    fun getAxisX(dest: Vector2 = Vector2()): Vector2 {
        return dest.set(m00, m10)
    }

    fun getAxisY(dest: Vector2 = Vector2()): Vector2 {
        return dest.set(m01, m11)
    }

    fun setAbsolute(): Matrix2x2 {
        m00 = abs(m00)
        m01 = abs(m01)
        m10 = abs(m10)
        m11 = abs(m11)
        return this
    }

    fun mul(other: Matrix2x2): Matrix2x2 {
        val nm00 = m00 * other.m00 + m01 * other.m10
        val nm01 = m00 * other.m01 + m01 * other.m11
        val nm10 = m10 * other.m00 + m11 * other.m10
        val nm11 = m10 * other.m01 + m11 * other.m11
        m00 = nm00
        m01 = nm01
        m10 = nm10
        m11 = nm11
        return this
    }

    fun transform(point: Vector2) = transform(point.x, point.y) { x, y -> point.set(x, y) }

    inline fun <R> transform(x: Float, y: Float, block: (Float, Float) -> R): R {
        val nx = transformX(x, y)
        val ny = transformY(x, y)
        return block(nx, ny)
    }

    fun transformX(x: Float, y: Float) = m00 * x + m01 * y

    fun transformY(x: Float, y: Float) = m10 * x + m11 * y

    override fun reset() {
        setIdentity()
    }

    fun copy() = Matrix2x2().set(this)
}

operator fun Matrix2x2.times(v: Vector2) = transform(v.cpy())

fun Matrix2x2.transposed() = copy().transpose()

operator fun Matrix2x2.times(m: Matrix2x2) = copy().mul(m)
