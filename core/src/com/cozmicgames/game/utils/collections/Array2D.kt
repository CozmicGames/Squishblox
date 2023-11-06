package com.cozmicgames.game.utils.collections

import com.badlogic.gdx.utils.Disposable
import kotlin.math.min

class Array2D<T>(val width: Int, val height: Int, block: ((Int, Int) -> T)? = null) : Disposable {
    constructor(size: Int, block: ((Int, Int) -> T)? = null) : this(size, size, block)

    private val data = arrayOfNulls<Any>(width * height)

    init {
        block?.let {
            repeat(width) { x ->
                repeat(height) { y ->
                    this[x, y] = it(x, y)
                }
            }
        }
    }

    private fun getIndex(x: Int, y: Int) = x + y * width

    operator fun set(x: Int, y: Int, value: T?) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return

        data[getIndex(x, y)] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun get(x: Int, y: Int): T? {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return null

        return data[getIndex(x, y)] as T?
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

fun <T> Array2D<T>.set(x: Int, y: Int, array: Array2D<T>, width: Int = array.width, height: Int = array.height, offsetX: Int = 0, offsetY: Int = 0) {
    val x1 = x + min(width, array.width - offsetX)
    val y1 = y + min(height, array.height - offsetY)

    for (xx in x until x1) {
        if (xx < 0 || xx >= this.width)
            continue

        for (yy in y until y1) {
            if (yy < 0 || yy >= this.height)
                continue

            this[xx, yy] = array[xx - x + offsetX, yy - y + offsetY]
        }
    }
}
