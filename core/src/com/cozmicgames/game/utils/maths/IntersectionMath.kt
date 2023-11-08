package com.cozmicgames.game.utils.maths

import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.max

fun intersectPointRect(x: Float, y: Float, minX: Float, minY: Float, maxX: Float, maxY: Float): Boolean {
    return minX <= x && minY <= y && maxX >= x && maxY >= y
}

fun intersectPointRect(x: Int, y: Int, minX: Int, minY: Int, maxX: Int, maxY: Int): Boolean {
    return minX <= x && minY <= y && maxX >= x && maxY >= y
}

fun intersectRectRect(minX0: Float, minY0: Float, maxX0: Float, maxY0: Float, minX1: Float, minY1: Float, maxX1: Float, maxY1: Float): Boolean {
    return maxX0 >= minX1 && maxY0 >= minY1 && minX0 <= maxX1 && minY0 <= maxY1
}

fun intersectRectRect(minX0: Int, minY0: Int, maxX0: Int, maxY0: Int, minX1: Int, minY1: Int, maxX1: Int, maxY1: Int): Boolean {
    return maxX0 >= minX1 && maxY0 >= minY1 && minX0 <= maxX1 && minY0 <= maxY1
}

fun intersectRectCirlce(minX: Float, minY: Float, maxX: Float, maxY: Float, centerX: Float, centerY: Float, radius: Float): Boolean {
    var radiusSquared = radius * radius
    if (centerX < minX) {
        val d = centerX - minX
        radiusSquared -= d * d
    } else if (centerX > maxX) {
        val d = centerX - maxX
        radiusSquared -= d * d
    }

    if (centerY < minY) {
        val d = centerY - minY
        radiusSquared -= d * d
    } else if (centerY > maxY) {
        val d = centerY - maxY
        radiusSquared -= d * d
    }

    return radiusSquared >= 0.0f
}

fun intersectPointTriangle(x: Float, y: Float, x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
    val b0 = (x - x1) * (y0 - y1) - (x0 - x1) * (y - y1) < 0.0f
    val b1 = (x - x2) * (y1 - y2) - (x1 - x2) * (y - y2) < 0.0f
    if (b0 != b1)
        return false
    val b2 = (x - x0) * (y2 - y0) - (x2 - x0) * (y - y0) < 0.0f
    return b1 == b2
}

fun intersectPointCircle(x: Float, y: Float, centerX: Float, centerY: Float, radius: Float): Boolean {
    val dx = x - centerX
    val dy = y - centerY
    return dx * dx + dy * dy <= radius * radius
}

fun intersectCircleCircle(x0: Float, y0: Float, radius0: Float, x1: Float, y1: Float, radius1: Float): Boolean {
    val dx = x1 - x0
    val dy = y1 - y0
    val r = radius0 + radius1
    return dx * dx + dy * dy < r * r
}

fun intersectRayRect(originX: Float, originY: Float, dirX: Float, dirY: Float, minX: Float, minY: Float, maxX: Float, maxY: Float, result: Vector2?): Boolean {
    val invDirX = 1.0f / dirX
    val invDirY = 1.0f / dirY
    var tNear: Float
    var tFar: Float
    val tymin: Float
    val tymax: Float

    if (invDirX >= 0.0f) {
        tNear = (minX - originX) * invDirX
        tFar = (maxX - originX) * invDirX
    } else {
        tNear = (maxX - originX) * invDirX
        tFar = (minX - originX) * invDirX
    }

    if (invDirY >= 0.0f) {
        tymin = (minY - originY) * invDirY
        tymax = (maxY - originY) * invDirY
    } else {
        tymin = (maxY - originY) * invDirY
        tymax = (minY - originY) * invDirY
    }

    if (tNear > tymax || tymin > tFar)
        return false

    tNear = if (tymin > tNear || tNear.isNaN()) tymin else tNear
    tFar = if (tymax < tFar || tFar.isNaN()) tymax else tFar

    if (tNear <= tFar && tFar >= 0.0f) {
        result?.x = tNear
        result?.y = tFar
        return true
    }

    return false
}

fun findSeparatingAxis(verticesA: Array<Vector2>, verticesB: Array<Vector2>, x: Float, y: Float): Boolean {
    var minA = Float.POSITIVE_INFINITY
    var maxA = Float.NEGATIVE_INFINITY
    var minB = Float.POSITIVE_INFINITY
    var maxB = Float.NEGATIVE_INFINITY
    val maxCount = max(verticesA.size, verticesB.size)
    repeat(maxCount) {
        if (it < verticesA.size) {
            val v = verticesA[it]
            val d = v.x * x + v.y * y
            if (d < minA)
                minA = d
            if (d > maxA)
                maxA = d
        }
        if (it < verticesB.size) {
            val v = verticesB[it]
            val d = v.x * x + v.y * y
            if (d < minB)
                minB = d
            if (d > maxB)
                maxB = d
        }
        if (minA <= maxB && minB <= maxA) {
            return false
        }
    }
    return true
}

fun testPolygonPolygon(verticesA: Array<Vector2>, verticesB: Array<Vector2>): Boolean {
    var i = 0
    var j = verticesA.size - 1
    while (i < verticesA.size) {
        val s = verticesA[i]
        val t = verticesA[j]
        if (findSeparatingAxis(verticesA, verticesB, s.y - t.y, t.x - s.x))
            return false
        j = i
        i++
    }

    i = 0
    j = verticesB.size - 1
    while (i < verticesB.size) {
        val s = verticesB[i]
        val t = verticesB[j]
        if (findSeparatingAxis(verticesA, verticesB, s.y - t.y, t.x - s.x))
            return false
        j = i
        i++
    }
    return true
}

fun intersectAabbAabb(minX0: Float, minY0: Float, minZ0: Float, maxX0: Float, maxY0: Float, maxZ0: Float, minX1: Float, minY1: Float, minZ1: Float, maxX1: Float, maxY1: Float, maxZ1: Float): Boolean {
    return maxX0 >= minX1 && maxY0 >= minY1 && maxZ0 >= minZ1 && minX0 <= maxX1 && minY0 <= maxY1 && minZ0 <= maxZ1
}

fun intersectObbObb(b0cX: Float, b0cY: Float, b0cZ: Float, b0uXx: Float, b0uXy: Float, b0uXz: Float, b0uYx: Float, b0uYy: Float, b0uYz: Float, b0uZx: Float, b0uZy: Float, b0uZz: Float, b0hsX: Float, b0hsY: Float, b0hsZ: Float, b1cX: Float, b1cY: Float, b1cZ: Float, b1uXx: Float, b1uXy: Float, b1uXz: Float, b1uYx: Float, b1uYy: Float, b1uYz: Float, b1uZx: Float, b1uZy: Float, b1uZz: Float, b1hsX: Float, b1hsY: Float, b1hsZ: Float): Boolean {
    var rb: Float
    val rm00 = b0uXx * b1uXx + b0uYx * b1uYx + b0uZx * b1uZx
    val rm10 = b0uXx * b1uXy + b0uYx * b1uYy + b0uZx * b1uZy
    val rm20 = b0uXx * b1uXz + b0uYx * b1uYz + b0uZx * b1uZz
    val rm01 = b0uXy * b1uXx + b0uYy * b1uYx + b0uZy * b1uZx
    val rm11 = b0uXy * b1uXy + b0uYy * b1uYy + b0uZy * b1uZy
    val rm21 = b0uXy * b1uXz + b0uYy * b1uYz + b0uZy * b1uZz
    val rm02 = b0uXz * b1uXx + b0uYz * b1uYx + b0uZz * b1uZx
    val rm12 = b0uXz * b1uXy + b0uYz * b1uYy + b0uZz * b1uZy
    val rm22 = b0uXz * b1uXz + b0uYz * b1uYz + b0uZz * b1uZz
    val EPSILON = 1E-5f
    val arm00 = abs(rm00) + EPSILON
    val arm01 = abs(rm01) + EPSILON
    val arm02 = abs(rm02) + EPSILON
    val arm10 = abs(rm10) + EPSILON
    val arm11 = abs(rm11) + EPSILON
    val arm12 = abs(rm12) + EPSILON
    val arm20 = abs(rm20) + EPSILON
    val arm21 = abs(rm21) + EPSILON
    val arm22 = abs(rm22) + EPSILON
    val tx = b1cX - b0cX
    val ty = b1cY - b0cY
    val tz = b1cZ - b0cZ
    val tax = tx * b0uXx + ty * b0uXy + tz * b0uXz
    val tay = tx * b0uYx + ty * b0uYy + tz * b0uYz
    val taz = tx * b0uZx + ty * b0uZy + tz * b0uZz
    var ra = b0hsX
    rb = b1hsX * arm00 + b1hsY * arm01 + b1hsZ * arm02
    if (abs(tax) > ra + rb) return false
    ra = b0hsY
    rb = b1hsX * arm10 + b1hsY * arm11 + b1hsZ * arm12
    if (abs(tay) > ra + rb) return false
    ra = b0hsZ
    rb = b1hsX * arm20 + b1hsY * arm21 + b1hsZ * arm22
    if (abs(taz) > ra + rb) return false
    ra = b0hsX * arm00 + b0hsY * arm10 + b0hsZ * arm20
    rb = b1hsX
    if (abs(tax * rm00 + tay * rm10 + taz * rm20) > ra + rb) return false
    ra = b0hsX * arm01 + b0hsY * arm11 + b0hsZ * arm21
    rb = b1hsY
    if (abs(tax * rm01 + tay * rm11 + taz * rm21) > ra + rb) return false
    ra = b0hsX * arm02 + b0hsY * arm12 + b0hsZ * arm22
    rb = b1hsZ
    if (abs(tax * rm02 + tay * rm12 + taz * rm22) > ra + rb) return false
    ra = b0hsY * arm20 + b0hsZ * arm10
    rb = b1hsY * arm02 + b1hsZ * arm01
    if (abs(taz * rm10 - tay * rm20) > ra + rb) return false
    ra = b0hsY * arm21 + b0hsZ * arm11
    rb = b1hsX * arm02 + b1hsZ * arm00
    if (abs(taz * rm11 - tay * rm21) > ra + rb) return false
    ra = b0hsY * arm22 + b0hsZ * arm12
    rb = b1hsX * arm01 + b1hsY * arm00
    if (abs(taz * rm12 - tay * rm22) > ra + rb) return false
    ra = b0hsX * arm20 + b0hsZ * arm00
    rb = b1hsY * arm12 + b1hsZ * arm11
    if (abs(tax * rm20 - taz * rm00) > ra + rb) return false
    ra = b0hsX * arm21 + b0hsZ * arm01
    rb = b1hsX * arm12 + b1hsZ * arm10
    if (abs(tax * rm21 - taz * rm01) > ra + rb) return false
    ra = b0hsX * arm22 + b0hsZ * arm02
    rb = b1hsX * arm11 + b1hsY * arm10
    if (abs(tax * rm22 - taz * rm02) > ra + rb) return false
    ra = b0hsX * arm10 + b0hsY * arm00
    rb = b1hsY * arm22 + b1hsZ * arm21
    if (abs(tay * rm00 - tax * rm10) > ra + rb) return false
    ra = b0hsX * arm11 + b0hsY * arm01
    rb = b1hsX * arm22 + b1hsZ * arm20
    if (abs(tay * rm01 - tax * rm11) > ra + rb) return false
    ra = b0hsX * arm12 + b0hsY * arm02
    rb = b1hsX * arm21 + b1hsY * arm20
    return abs(tay * rm02 - tax * rm12) <= ra + rb
}

fun intersectRayAabb(originX: Float, originY: Float, originZ: Float, dirX: Float, dirY: Float, dirZ: Float, minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, result: Vector2?): Boolean {
    val invDirX = 1.0f / dirX
    val invDirY = 1.0f / dirY
    val invDirZ = 1.0f / dirZ
    var tNear: Float
    var tFar: Float
    val tymin: Float
    val tymax: Float
    val tzmin: Float
    val tzmax: Float

    if (invDirX >= 0.0f) {
        tNear = (minX - originX) * invDirX
        tFar = (maxX - originX) * invDirX
    } else {
        tNear = (maxX - originX) * invDirX
        tFar = (minX - originX) * invDirX
    }

    if (invDirY >= 0.0f) {
        tymin = (minY - originY) * invDirY
        tymax = (maxY - originY) * invDirY
    } else {
        tymin = (maxY - originY) * invDirY
        tymax = (minY - originY) * invDirY
    }

    if (tNear > tymax || tymin > tFar)
        return false

    if (invDirZ >= 0.0f) {
        tzmin = (minZ - originZ) * invDirZ
        tzmax = (maxZ - originZ) * invDirZ
    } else {
        tzmin = (maxZ - originZ) * invDirZ
        tzmax = (minZ - originZ) * invDirZ
    }

    if (tNear > tzmax || tzmin > tFar)
        return false

    tNear = if (tymin > tNear || tNear.isNaN()) tymin else tNear
    tFar = if (tymax < tFar || tFar.isNaN()) tymax else tFar
    tNear = if (tzmin > tNear) tzmin else tNear
    tFar = if (tzmax < tFar) tzmax else tFar

    if (tNear < tFar && tFar >= 0.0f) {
        result?.x = tNear
        result?.y = tFar
        return true
    }


    return false
}

fun intersectAabbSphere(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, centerX: Float, centerY: Float, centerZ: Float, radius: Float): Boolean {
    var radiusSquared = radius * radius
    if (centerX < minX) {
        val d = centerX - minX
        radiusSquared -= d * d
    } else if (centerX > maxX) {
        val d = centerX - maxX
        radiusSquared -= d * d
    }
    if (centerY < minY) {
        val d = centerY - minY
        radiusSquared -= d * d
    } else if (centerY > maxY) {
        val d = centerY - maxY
        radiusSquared -= d * d
    }
    if (centerZ < minZ) {
        val d = centerZ - minZ
        radiusSquared -= d * d
    } else if (centerZ > maxZ) {
        val d = centerZ - maxZ
        radiusSquared -= d * d
    }
    return radiusSquared >= 0.0
}
