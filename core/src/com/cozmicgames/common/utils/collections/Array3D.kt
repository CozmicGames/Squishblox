package com.cozmicgames.common.utils.collections

import com.badlogic.gdx.utils.Disposable
import kotlin.math.min

class Array3D<T>(val width: Int, val height: Int, val depth: Int, block: ((Int, Int, Int) -> T)? = null) : Disposable {
    constructor(size: Int, block: ((Int, Int, Int) -> T)? = null) : this(size, size, size, block)

    private val data = arrayOfNulls<Any>(width * height * depth)

    init {
        block?.let {
            repeat(width) { x ->
                repeat(height) { y ->
                    repeat(depth) { z ->
                        this[x, y, z] = it(x, y, z)
                    }
                }
            }
        }
    }

    private fun getIndex(x: Int, y: Int, z: Int) = x + y * width + z * height * height

    operator fun set(x: Int, y: Int, z: Int, value: T?) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
            return

        data[getIndex(x, y, z)] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun get(x: Int, y: Int, z: Int): T? {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
            return null

        return data[getIndex(x, y, z)] as T?
    }

    fun fill(value: T?) {
        data.fill(value)
    }

    fun clear() = fill(null)

    override fun dispose() {
        data.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}

fun <T> Array3D<T>.set(x: Int, y: Int, z: Int, array: Array3D<T>, width: Int = array.width, height: Int = array.height, depth: Int = array.depth, offsetX: Int = 0, offsetY: Int = 0, offsetZ: Int = 0) {
    val x1 = x + min(width, array.width - offsetX)
    val y1 = y + min(height, array.height - offsetY)
    val z1 = z + min(depth, array.depth - offsetZ)

    for (xx in x until x1) {
        if (xx < 0 || xx >= this.width)
            continue

        for (yy in y until y1) {
            if (yy < 0 || yy >= this.height)
                continue

            for (zz in z until z1) {
                if (zz < 0 || zz >= this.depth)
                    continue

                this[xx, yy, zz] = array[xx - x + offsetX, yy - y + offsetY, zz - z + offsetZ]
            }
        }
    }
}