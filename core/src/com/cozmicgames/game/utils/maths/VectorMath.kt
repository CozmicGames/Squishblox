package com.cozmicgames.game.utils.maths

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.sqrt

fun dot(x0: Float, y0: Float, x1: Float, y1: Float) = x0 * x1 + y0 * y1

fun dot(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float) = x0 * x1 + y0 * y1 + z0 * z1

fun dot(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float) = x0 * x1 + y0 * y1 + z0 * z1 + w0 * w1

fun lengthSquared(x: Float, y: Float) = dot(x, y, x, y)

fun lengthSquared(x: Float, y: Float, z: Float) = dot(x, y, z, x, y, z)

fun lengthSquared(x: Float, y: Float, z: Float, w: Float) = dot(x, y, z, w, x, y, z, w)

fun length(x: Float, y: Float) = sqrt(lengthSquared(x, y))

fun length(x: Float, y: Float, z: Float) = sqrt(lengthSquared(x, y, z))

fun length(x: Float, y: Float, z: Float, w: Float) = sqrt(lengthSquared(x, y, z, w))

fun distanceSquared(x0: Float, y0: Float, x1: Float, y1: Float) = lengthSquared(x1 - x0, y1 - y0)

fun distanceSquared(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float) = lengthSquared(x1 - x0, y1 - y0, z1 - z0)

fun distanceSquared(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float) = lengthSquared(x1 - x0, y1 - y0, z1 - z0, w1 - w0)

fun distance(x0: Float, y0: Float, x1: Float, y1: Float) = sqrt(distanceSquared(x0, y0, x1, y1))

fun distance(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float) = sqrt(distanceSquared(x0, y0, z0, x1, y1, z1))

fun distance(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float) = sqrt(distanceSquared(x0, y0, z0, w0, x1, y1, z1, w1))

fun <R> perpendicular(x: Float, y: Float, block: (Float, Float) -> R): R {
    val negx = -x
    return block(y, negx)
}

fun perpendicular(x: Float, y: Float) = perpendicular(x, y) { nx, ny -> Vector2(nx, ny) }

fun <R> cross(x: Float, y: Float, s: Float, block: (Float, Float) -> R): R {
    val nx = -s * y
    val ny = s * x
    return block(nx, ny)
}

fun cross(v: Vector2, s: Float, dest: Vector2 = Vector2()) = cross(v.x, v.y, s, dest)

fun cross(x: Float, y: Float, s: Float, dest: Vector2 = Vector2()) = cross(x, y, s) { nx, ny -> dest.set(nx, ny) }

fun cross(x0: Float, y0: Float, x1: Float, y1: Float) = x0 * y1 - y0 * x1

fun <R> cross(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val nx = y0 * z1 - z0 * y1
    val ny = z0 * x1 - x0 * z1
    val nz = x0 * y1 - y0 * x1
    return block(nx, ny, nz)
}

fun cross(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = cross(x0, y0, z0, x1, y1, z1) { nx, ny, nz -> dest.set(nx, ny, nz) }

fun lengthSquared(v: Vector2) = lengthSquared(v.x, v.y)

fun lengthSquared(v: Vector3) = lengthSquared(v.x, v.y, v.z)

fun lengthSquared(x: Int, y: Int) = x * x + y * y

fun lengthSquared(x: Int, y: Int, z: Int) = x * x + y * y + z * z

fun lengthSquared(x: Int, y: Int, z: Int, w: Int) = x * x + y * y + z * z + w * w

fun length(v: Vector2) = length(v.x, v.y)

fun length(v: Vector3) = length(v.x, v.y, v.z)

fun length(x: Int, y: Int) = sqrt(lengthSquared(x, y).toFloat()).toInt()

fun length(x: Int, y: Int, z: Int) = sqrt(lengthSquared(x, y, z).toFloat()).toInt()

fun length(x: Int, y: Int, z: Int, w: Int) = sqrt(lengthSquared(x, y, z, w).toFloat()).toInt()

fun distanceSquared(v0: Vector2, v1: Vector2) = distanceSquared(v0.x, v0.y, v1.x, v1.y)

fun distanceSquared(v0: Vector3, v1: Vector3) = distanceSquared(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z)

fun distanceSquared(x0: Int, y0: Int, x1: Int, y1: Int) = lengthSquared(x1 - x0, y1 - y0)

fun distanceSquared(x0: Int, y0: Int, z0: Int, x1: Int, y1: Int, z1: Int) = lengthSquared(x1 - x0, y1 - y0, z1 - z0)

fun distanceSquared(x0: Int, y0: Int, z0: Int, w0: Int, x1: Int, y1: Int, z1: Int, w1: Int) = lengthSquared(x1 - x0, y1 - y0, z1 - z0, w1 - w0)

fun distance(v0: Vector2, v1: Vector2) = distance(v0.x, v0.y, v1.x, v1.y)

fun distance(v0: Vector3, v1: Vector3) = distance(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z)

fun distance(x0: Int, y0: Int, x1: Int, y1: Int) = sqrt(distanceSquared(x0, y0, x1, y1).toFloat()).toInt()

fun distance(x0: Int, y0: Int, z0: Int, x1: Int, y1: Int, z1: Int) = sqrt(distanceSquared(x0, y0, z0, x1, y1, z1).toFloat()).toInt()

fun distance(x0: Int, y0: Int, z0: Int, w0: Int, x1: Int, y1: Int, z1: Int, w1: Int) = sqrt(distanceSquared(x0, y0, z0, w0, x1, y1, z1, w1).toFloat()).toInt()

fun <R> normalized(x: Float, y: Float, block: (Float, Float) -> R): R {
    val factor = 1.0f / length(x, y)
    return block(x * factor, y * factor)
}

fun <R> normalized(x: Float, y: Float, z: Float, block: (Float, Float, Float) -> R): R {
    val factor = 1.0f / length(x, y, z)
    return block(x * factor, y * factor, z * factor)
}

fun <R> normalized(x: Float, y: Float, z: Float, w: Float, block: (Float, Float, Float, Float) -> R): R {
    val factor = 1.0f / length(x, y, z, w)
    return block(x * factor, y * factor, z * factor, w * factor)
}

fun plus(a: Vector2, b: Vector2, dest: Vector2 = Vector2()) = plus(a.x, a.y, b.x, b.y, dest)

fun plus(a: Vector3, b: Vector3, dest: Vector3 = Vector3()) = plus(a.x, a.y, a.z, b.x, b.y, b.z, dest)

fun plus(x0: Float, y0: Float, x1: Float, y1: Float, dest: Vector2 = Vector2()) = plus(x0, y0, x1, y1) { x, y -> dest.set(x, y) }

fun plus(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = plus(x0, y0, z0, x1, y1, z1) { x, y, z -> dest.set(x, y, z) }

fun <R> plus(x0: Float, y0: Float, x1: Float, y1: Float, block: (Float, Float) -> R): R {
    val x = x1 + x0
    val y = y1 + y0
    return block(x, y)
}

fun <R> plus(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val x = x1 + x0
    val y = y1 + y0
    val z = z1 + z0
    return block(x, y, z)
}

fun <R> plus(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, block: (Float, Float, Float, Float) -> R): R {
    val x = x1 + x0
    val y = y1 + y0
    val z = z1 + z0
    val w = w1 + w0
    return block(x, y, z, w)
}

fun sub(a: Vector2, b: Vector2, dest: Vector2 = Vector2()) = sub(a.x, a.y, b.x, b.y, dest)

fun sub(a: Vector3, b: Vector3, dest: Vector3 = Vector3()) = sub(a.x, a.y, a.z, b.x, b.y, b.z, dest)

fun sub(x0: Float, y0: Float, x1: Float, y1: Float, dest: Vector2 = Vector2()) = sub(x0, y0, x1, y1) { x, y -> dest.set(x, y) }

fun sub(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = sub(x0, y0, z0, x1, y1, z1) { x, y, z -> dest.set(x, y, z) }

fun <R> sub(x0: Float, y0: Float, x1: Float, y1: Float, block: (Float, Float) -> R): R {
    val x = x1 - x0
    val y = y1 - y0
    return block(x, y)
}

fun <R> sub(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val x = x1 - x0
    val y = y1 - y0
    val z = z1 - z0
    return block(x, y, z)
}

fun <R> sub(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, block: (Float, Float, Float, Float) -> R): R {
    val x = x1 - x0
    val y = y1 - y0
    val z = z1 - z0
    val w = w1 - w0
    return block(x, y, z, w)
}

fun times(a: Vector2, b: Vector2, dest: Vector2 = Vector2()) = times(a.x, a.y, b.x, b.y, dest)

fun times(a: Vector3, b: Vector3, dest: Vector3 = Vector3()) = times(a.x, a.y, a.z, b.x, b.y, b.z, dest)

fun times(x0: Float, y0: Float, x1: Float, y1: Float, dest: Vector2 = Vector2()) = times(x0, y0, x1, y1) { x, y -> dest.set(x, y) }

fun times(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = times(x0, y0, z0, x1, y1, z1) { x, y, z -> dest.set(x, y, z) }

fun <R> times(x0: Float, y0: Float, x1: Float, y1: Float, block: (Float, Float) -> R): R {
    val x = x1 * x0
    val y = y1 * y0
    return block(x, y)
}

fun <R> times(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val x = x1 * x0
    val y = y1 * y0
    val z = z1 * z0
    return block(x, y, z)
}

fun <R> times(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, block: (Float, Float, Float, Float) -> R): R {
    val x = x1 * x0
    val y = y1 * y0
    val z = z1 * z0
    val w = w1 * w0
    return block(x, y, z, w)
}

fun div(a: Vector2, b: Vector2, dest: Vector2 = Vector2()) = div(a.x, a.y, b.x, b.y, dest)

fun div(a: Vector3, b: Vector3, dest: Vector3 = Vector3()) = div(a.x, a.y, a.z, b.x, b.y, b.z, dest)

fun div(x0: Float, y0: Float, x1: Float, y1: Float, dest: Vector2 = Vector2()) = div(x0, y0, x1, y1) { x, y -> dest.set(x, y) }

fun div(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = div(x0, y0, z0, x1, y1, z1) { x, y, z -> dest.set(x, y, z) }

fun <R> div(x0: Float, y0: Float, x1: Float, y1: Float, block: (Float, Float) -> R): R {
    val x = x0 / x1
    val y = y0 / y1
    return block(x, y)
}

fun <R> div(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val x = x0 / x1
    val y = y0 / y1
    val z = z0 / z1
    return block(x, y, z)
}

fun <R> div(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, block: (Float, Float, Float, Float) -> R): R {
    val x = x0 / x1
    val y = y0 / y1
    val z = z0 / z1
    val w = w0 / w1
    return block(x, y, z, w)
}
