package com.cozmicgames.game.utils.maths

import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.utils.collections.Resettable
import com.cozmicgames.game.utils.extensions.element
import kotlin.math.cos
import kotlin.math.sin

class Matrix3x2 : Resettable {
    val data = Array(6) { 0.0f }

    var m00 by data.element(0)
    var m01 by data.element(1)
    var m02 by data.element(2)
    var m10 by data.element(3)
    var m11 by data.element(4)
    var m12 by data.element(5)

    init {
        setIdentity()
    }

    fun set(transform: Matrix3x2): Matrix3x2 {
        m00 = transform.m00
        m01 = transform.m01
        m02 = transform.m02
        m10 = transform.m10
        m11 = transform.m11
        m12 = transform.m12
        return this
    }

    fun setIdentity(): Matrix3x2 {
        m00 = 1.0f
        m01 = 0.0f
        m02 = 0.0f
        m00 = 0.0f
        m11 = 1.0f
        m12 = 0.0f
        return this
    }

    fun setZero(): Matrix3x2 {
        m00 = 0.0f
        m01 = 0.0f
        m02 = 0.0f
        m00 = 0.0f
        m11 = 0.0f
        m12 = 0.0f
        return this
    }

    fun setToTranslation(translation: Vector2) = setToTranslation(translation.x, translation.y)

    fun setToTranslation(x: Float, y: Float): Matrix3x2 {
        m00 = 1.0f
        m01 = 0.0f
        m02 = x
        m00 = 0.0f
        m11 = 1.0f
        m12 = y
        return this
    }

    fun setToScaling(scaling: Vector2) = setToScaling(scaling.x, scaling.y)

    fun setToScaling(x: Float, y: Float): Matrix3x2 {
        m00 = x
        m01 = 0.0f
        m02 = 0.0f
        m00 = 0.0f
        m11 = y
        m12 = 0.0f
        return this
    }

    fun setToRotation(radians: Float): Matrix3x2 {
        val cos = cos(radians)
        val sin = sin(radians)
        m00 = cos
        m01 = -sin
        m02 = 0.0f
        m10 = sin
        m11 = cos
        m12 = 0.0f
        return this
    }

    fun setToShearing(shearing: Vector2) = setToShearing(shearing.x, shearing.y)

    fun setToShearing(x: Float, y: Float): Matrix3x2 {
        m00 = 1.0f
        m01 = x
        m02 = 0.0f
        m10 = y
        m11 = 1.0f
        m12 = 0.0f
        return this
    }

    fun setToTranslationRotationScaling(translation: Vector2, rotation: Float, scaling: Vector2) = setToTranslationRotationScaling(translation.x, translation.y, rotation, scaling.x, scaling.y)

    fun setToTranslationRotationScaling(x: Float, y: Float, rotation: Float, scaleX: Float, scaleY: Float): Matrix3x2 {
        m02 = x
        m12 = y

        if (rotation == 0.0f) {
            m00 = scaleX
            m01 = 0f
            m10 = 0f
            m11 = scaleY
        } else {
            val sin = sin(rotation)
            val cos = cos(rotation)
            m00 = cos * scaleX
            m01 = -sin * scaleY
            m10 = sin * scaleX
            m11 = cos * scaleY
        }
        return this
    }

    fun mul(other: Matrix3x2): Matrix3x2 {
        val tmp00 = m00 * other.m00 + m01 * other.m10
        val tmp01 = m00 * other.m01 + m01 * other.m11
        val tmp02 = m00 * other.m02 + m01 * other.m12 + m02
        val tmp10 = m10 * other.m00 + m11 * other.m10
        val tmp11 = m10 * other.m01 + m11 * other.m11
        val tmp12 = m10 * other.m02 + m11 * other.m12 + m12
        m00 = tmp00
        m01 = tmp01
        m02 = tmp02
        m10 = tmp10
        m11 = tmp11
        m12 = tmp12
        return this
    }

    fun transform(point: Vector2) = transform(point.x, point.y) { x, y -> point.set(x, y) }

    inline fun <R> transform(x: Float, y: Float, block: (Float, Float) -> R): R {
        val nx = transformX(x, y)
        val ny = transformY(x, y)
        return block(nx, ny)
    }

    fun transformX(x: Float, y: Float) = m00 * x + m01 * y + m02

    fun transformY(x: Float, y: Float) = m10 * x + m11 * y + m12

    override fun reset() {
        setIdentity()
    }

    fun copy() = Matrix3x2().set(this)
}
