package com.cozmicgames.game.world

import com.cozmicgames.game.utils.maths.intersectPointRect
import com.cozmicgames.game.utils.maths.intersectRectRect

class World {
    private class Block(var id: Int, var minX: Int, var minY: Int, var maxX: Int, var maxY: Int)

    private val blocks = arrayListOf<Block>()

    private var currentId = 0

    fun createBlock(minX: Int, minY: Int, maxX: Int, maxY: Int): Int {
        val id = currentId++
        val block = Block(id, minX, minY, maxX, maxY)
        blocks += block
        return id
    }

    fun updateBlock(id: Int, minX: Int, minY: Int, maxX: Int, maxY: Int) {
        val block = blocks.find { it.id == id } ?: return
        block.minX = minX
        block.minY = minY
        block.maxX = maxX
        block.maxY = maxY
    }

    fun getBlock(x: Int, y: Int): Int? {
        return blocks.find { intersectPointRect(x.toFloat(), y.toFloat(), it.minX.toFloat(), it.minY.toFloat(), it.maxX.toFloat(), it.maxY.toFloat()) }?.id
    }

    fun getBlocks(minX: Int, minY: Int, maxX: Int, maxY: Int, idFilter: (Int) -> Boolean = { true }): List<Int> {
        var list: ArrayList<Int>? = null
        forEachBlock(minX, minY, maxX, maxY, idFilter) {
            if (list == null)
                list = arrayListOf()
            list!! += it
        }
        return list ?: emptyList()
    }

    fun forEachBlock(minX: Int, minY: Int, maxX: Int, maxY: Int, idFilter: (Int) -> Boolean = { true }, block: (Int) -> Unit) {
        blocks.forEach {
            if (idFilter(it.id) && intersectRectRect(minX.toFloat(), minY.toFloat(), maxX.toFloat(), maxY.toFloat(), it.minX.toFloat(), it.minY.toFloat(), it.maxX.toFloat(), it.maxY.toFloat()))
                block(it.id)
        }
    }

    fun removeBlock(id: Int) {
        blocks.removeIf { it.id == id }
    }

    fun isCellFilled(x: Int, y: Int, idFilter: (Int) -> Boolean = { true }): Boolean {
        return blocks.any {
            if (idFilter(it.id))
                intersectPointRect(x.toFloat(), y.toFloat(), it.minX.toFloat(), it.minY.toFloat(), it.maxX.toFloat(), it.maxY.toFloat())
            else
                false
        }
    }
}