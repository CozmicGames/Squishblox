package com.cozmicgames.game.utils.maths

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.cozmicgames.game.utils.extensions.clamp
import kotlin.math.*
import kotlin.random.Random


const val PI = 3.1415926536f
const val HALF_PI = PI * 0.5f
const val TWO_PI = PI * 2.0f
const val FOUR_PI = PI * 4.0f
const val INV_PI = 1.0f / PI
const val INV_TWO_PI = INV_PI * 0.5f
const val INV_FOUR_PI = INV_PI * 0.25f

val FLOAT_EPSILON by lazy {
    var p = 0
    var e = 1.0f
    while (e + 1.0f != 1.0f) {
        e *= 0.5f
        p--
    }
    2.0f.pow(p + 1)
}

fun toDegrees(radians: Float) = radians * 180.0f / PI

fun toRadians(degrees: Float) = degrees * PI / 180.0f

fun Float.smoothstep(edge0: Float, edge1: Float): Float {
    val v = (this - edge0) / (edge1 - edge0)
    val step2 = v.clamp(0.0f, 1.0f)
    return step2 * step2 * (3 - 2 * step2)
}

fun Float.convertRange(minSrc: Float, maxSrc: Float, minDst: Float, maxDst: Float): Float = (((this - minSrc) / (maxSrc - minSrc)) * (maxDst - minDst)) + minDst

fun lerp(min: Float, max: Float, t: Float) = min + (max - min) * t

fun mix(a: Float, b: Float, x: Float) = a * (1.0f - x) + b * x

fun fract(v: Float) = v % 1

fun randomInt(range: IntRange? = null) = if (range == null) Random.nextInt() else Random.nextInt(range.first, range.last)

fun randomInt(until: Int) = Random.nextInt(until)

fun randomFloat() = Random.nextFloat()

fun randomBoolean() = Random.nextBoolean()

fun randomChance(probability: Float) = randomFloat() > probability

fun randomBytes(array: ByteArray, offset: Int = 0, length: Int = array.size - offset) = Random.nextBytes(array, offset, offset + length)

fun det(x0: Float, y0: Float, x1: Float, y1: Float) = x0 * y1 - y0 * x1

fun isCCW(p: Vector2, q: Vector2, r: Vector2) = det(q.x - p.x, q.y - p.y, r.x - p.x, r.y - p.y) >= 0.0f

fun isCCW(polygon: Array<Float>, offset: Int, count: Int): Boolean {
    return !isCW(polygon, offset, count)
}

fun isCW(polygon: Array<Float>, offset: Int, count: Int): Boolean {
    if (count <= 2)
        return false

    var area = 0.0f
    val last = offset + count - 2
    var x0 = polygon[last]
    var y0 = polygon[last + 1]
    var i = offset

    while (i <= last) {
        val x1 = polygon[i]
        val y1 = polygon[i + 1]
        area += x0 * y1 - x1 * y0
        x0 = x1
        y0 = y1
        i += 2
    }

    return area < 0
}

fun reverseVertices(polygon: Array<Float>, offset: Int, count: Int) {
    val lastX = offset + count - 2
    var i = offset
    val n = offset + count / 2

    while (i < n) {
        val other = lastX - i
        val x = polygon[i]
        val y = polygon[i + 1]
        polygon[i] = polygon[other]
        polygon[i + 1] = polygon[other + 1]
        polygon[other] = x
        polygon[other + 1] = y
        i += 2
    }
}

fun arePointsOnSameSide(a: Vector2, b: Vector2, c: Vector2, d: Vector2): Boolean {
    val px = d.x - c.x
    val py = d.y - c.y
    val l = det(px, py, a.x - c.x, a.y - c.y)
    val m = det(px, py, b.x - c.x, b.y - c.y)
    return l * m >= 0.0f
}

fun isPointInTriangle(p: Vector2, a: Vector2, b: Vector2, c: Vector2) = arePointsOnSameSide(p, a, b, c) && arePointsOnSameSide(p, b, a, c) && arePointsOnSameSide(p, c, a, b)

fun isAnyPointInTriangle(points: Iterable<Vector2>, a: Vector2, b: Vector2, c: Vector2): Boolean {
    for (point in points)
        if (point != a && point != b && point != c && isPointInTriangle(point, a, b, c))
            return true

    return false
}

fun isEar(points: Iterable<Vector2>, p: Vector2, q: Vector2, r: Vector2) = isCCW(p, q, r) && isAnyPointInTriangle(points, p, q, r)

fun fma(a: Float, b: Float, c: Float) = a * b + c

fun smoothDamp(current: Vector2, target: Vector2, currentVelocity: Vector2, smoothingTime: Float, maxSpeed: Float, deltaTime: Float, dest: Vector2 = Vector2()): Vector2 {
    var outputX: Float
    var outputY: Float
    val omega = 2.0f / max(FLOAT_EPSILON, smoothingTime)
    val x = omega * deltaTime
    val exp = 1.0f / (1.0f + x + 0.48f * x * x + 0.235f * x * x * x)
    var changeX = current.x - target.x
    var changeY = current.y - target.y
    val maxChange = maxSpeed * smoothingTime
    val maxChangeSq = maxChange * maxChange
    val sqrmag: Float = changeX * changeX + changeY * changeY
    if (sqrmag > maxChangeSq) {
        val mag = sqrt(sqrmag)
        changeX = changeX / mag * maxChange
        changeY = changeY / mag * maxChange
    }
    val targetX = current.x - changeX
    val targetY = current.y - changeY

    val tempX = (currentVelocity.x + omega * changeX) * deltaTime
    val tempY = (currentVelocity.y + omega * changeY) * deltaTime

    currentVelocity.x = (currentVelocity.x - omega * tempX) * exp
    currentVelocity.y = (currentVelocity.y - omega * tempY) * exp

    outputX = targetX + (changeX + tempX) * exp
    outputY = targetY + (changeY + tempY) * exp

    val origMinusCurrentX = target.x - current.x
    val origMinusCurrentY = target.y - current.y
    val outMinusOrigX = outputX - target.x
    val outMinusOrigY = outputY - target.y

    if (origMinusCurrentX * outMinusOrigX + origMinusCurrentY * outMinusOrigY > 0.0f) {
        outputX = target.x
        outputY = target.y
        currentVelocity.x = (outputX - target.x) / deltaTime
        currentVelocity.y = (outputY - target.y) / deltaTime
    }

    return dest.set(outputX, outputY)
}

fun smoothDamp(current: Vector3, target: Vector3, currentVelocity: Vector3, smoothingTime: Float, maxSpeed: Float, deltaTime: Float, dest: Vector3 = Vector3()): Vector3 {
    var outputX: Float
    var outputY: Float
    var outputZ: Float
    val omega = 2.0f / max(FLOAT_EPSILON, smoothingTime)
    val x = omega * deltaTime
    val exp = 1.0f / (1.0f + x + 0.48f * x * x + 0.235f * x * x * x)
    var changeX = current.x - target.x
    var changeY = current.y - target.y
    var changeZ = current.z - target.z
    val maxChange = maxSpeed * smoothingTime
    val maxChangeSq = maxChange * maxChange
    val sqrmag: Float = changeX * changeX + changeY * changeY + changeZ * changeZ
    if (sqrmag > maxChangeSq) {
        val mag = sqrt(sqrmag)
        changeX = changeX / mag * maxChange
        changeY = changeY / mag * maxChange
        changeZ = changeZ / mag * maxChange
    }
    val targetX = current.x - changeX
    val targetY = current.y - changeY
    val targetZ = current.z - changeZ

    val tempX = (currentVelocity.x + omega * changeX) * deltaTime
    val tempY = (currentVelocity.y + omega * changeY) * deltaTime
    val tempZ = (currentVelocity.z + omega * changeZ) * deltaTime

    currentVelocity.x = (currentVelocity.x - omega * tempX) * exp
    currentVelocity.y = (currentVelocity.y - omega * tempY) * exp
    currentVelocity.z = (currentVelocity.z - omega * tempZ) * exp

    outputX = targetX + (changeX + tempX) * exp
    outputY = targetY + (changeY + tempY) * exp
    outputZ = targetZ + (changeZ + tempZ) * exp

    val origMinusCurrentX = target.x - current.x
    val origMinusCurrentY = target.y - current.y
    val origMinusCurrentZ = target.z - current.z
    val outMinusOrigX = outputX - target.x
    val outMinusOrigY = outputY - target.y
    val outMinusOrigZ = outputZ - target.z

    if (origMinusCurrentX * outMinusOrigX + origMinusCurrentY * outMinusOrigY + origMinusCurrentZ * outMinusOrigZ > 0.0f) {
        outputX = target.x
        outputY = target.y
        outputZ = target.z
        currentVelocity.x = (outputX - target.x) / deltaTime
        currentVelocity.y = (outputY - target.y) / deltaTime
        currentVelocity.z = (outputZ - target.z) / deltaTime
    }

    return dest.set(outputX, outputY, outputZ)
}

fun forEachLinePoint(x0: Int, y0: Int, x1: Int, y1: Int, block: (Int, Int) -> Unit) {
    var d = 0
    val dx = abs(x1 - x0)
    val dy = abs(y1 - y0)
    val dx2 = dx shl 1
    val dy2 = dy shl 1
    val ix = if (x0 < x1) 1 else -1
    val iy = if (y0 < y1) 1 else -1
    var xx = x0
    var yy = y0
    if (dy <= dx)
        while (true) {
            block(xx, yy)
            if (xx == x1)
                break
            xx += ix
            d += dy2
            if (d > dx) {
                yy += iy
                d -= dx2
            }
        }
    else
        while (true) {
            block(xx, yy)
            if (yy == y1)
                break
            yy += iy
            d += dx2
            if (d > dy) {
                xx += ix
                d -= dy2
            }
        }
}

fun generatePoissonDiskSamplingPoints(radius: Float, minDistance: Float, tries: Int, random: Random = Random, callback: (Float, Float) -> Unit) {
    val radiusSquared = radius * radius
    val minDistanceSquared = minDistance * minDistance
    val cellSize = minDistance / sqrt(2.0).toFloat()
    val numCells = (radius * 2.0f / cellSize).toInt() + 1
    val grid = arrayOfNulls<Vector2>(numCells * numCells)
    val processList = arrayListOf<Vector2>()

    fun searchNeighbors(px: Float, py: Float): Boolean {
        val row = ((py + radius) / cellSize).toInt()
        val col = ((px + radius) / cellSize).toInt()

        if (grid[row * numCells + col] != null)
            return true

        val minX = max(0, col - 1)
        val minY = max(0, row - 1)
        val maxX = min(col + 1, numCells - 1)
        val maxY = min(row + 1, numCells - 1)

        repeat(maxY - minY) { yy ->
            val y = minY + yy

            repeat(maxX - minX) { xx ->
                val x = minX + xx

                val v = grid[y * numCells + x]

                if (v != null) {
                    val dx = v.x - px
                    val dy = v.y - py
                    if (dx * dx + dy * dy < minDistanceSquared) {
                        return true
                    }
                }
            }
        }

        return false
    }

    fun insert(p: Vector2) {
        val row = ((p.y + radius) / cellSize).toInt()
        val col = ((p.x + radius) / cellSize).toInt()
        grid[row * numCells + col] = p
    }

    var x: Float
    var y: Float
    do {
        x = random.nextFloat() * 2.0f - 1.0f
        y = random.nextFloat() * 2.0f - 1.0f
    } while (x * x + y * y > 1.0f)

    val initial = Vector2(x, y)

    processList.add(initial)
    callback(initial.x, initial.y)
    insert(initial)

    while (!processList.isEmpty()) {
        val i = random.nextInt(processList.size)
        val sample = processList[i]
        var found = false

        search@ for (s in (0 until tries)) {
            val angle = random.nextFloat() * TWO_PI
            val radius = minDistance * (random.nextFloat() + 1.0f)
            x = radius * sin(angle + HALF_PI)
            y = radius * sin(angle)
            x += sample.x
            y += sample.y
            if (x * x + y * y > radiusSquared)
                continue@search
            if (!searchNeighbors(x, y)) {
                found = true
                callback(x, y)
                val f = Vector2(x, y)
                processList.add(f)
                insert(f)
                break
            }
        }
        if (!found)
            processList.removeAt(i)
    }
}

fun generatePoissonDiskSamplingPoints(radius: Float, minDistance: Float, tries: Int, random: Random = Random, dest: MutableList<Vector2> = arrayListOf()): MutableList<Vector2> {
    generatePoissonDiskSamplingPoints(radius, minDistance, tries, random) { x, y -> dest.add(Vector2(x, y)) }
    return dest
}
