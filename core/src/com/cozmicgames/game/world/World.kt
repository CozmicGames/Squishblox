package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.IntMap
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.utils.collections.DynamicStack
import com.cozmicgames.game.utils.maths.intersectPointRect

class World {
    companion object {
        private const val DEBUG_INTERSECTION = false
    }

    private class Block(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int)

    private val blocks = IntMap<Block>()
    private val freeIds = DynamicStack<Int>()
    private var currentId = 0

    fun createBlock(minX: Int, minY: Int, maxX: Int, maxY: Int): Int {
        val id = if (!freeIds.isEmpty) freeIds.pop() else currentId++
        val block = Block(minX, minY, maxX, maxY)
        blocks.put(id, block)
        return id
    }

    fun updateBlock(id: Int, minX: Int, minY: Int, maxX: Int, maxY: Int) {
        val block = blocks.get(id) ?: return
        block.minX = minX
        block.minY = minY
        block.maxX = maxX
        block.maxY = maxY
    }

    fun getBlock(x: Int, y: Int): Int? {
        blocks.forEach {
            if (intersectPointRect(x.toFloat(), y.toFloat(), it.value.minX.toFloat(), it.value.minY.toFloat(), it.value.maxX.toFloat(), it.value.maxY.toFloat()))
                return it.key
        }
        return null
    }

    fun getBlocks(minX: Int, minY: Int, maxX: Int, maxY: Int, idFilter: (Int) -> Boolean = { true }): List<Int> {
        if (DEBUG_INTERSECTION)
            Game.graphics2d.submit<DirectRenderable2D> {
                it.layer = 80
                it.texture = "blank"
                it.x = minX * WorldConstants.WORLD_CELL_SIZE
                it.y = minY * WorldConstants.WORLD_CELL_SIZE
                it.width = (maxX - minX) * WorldConstants.WORLD_CELL_SIZE
                it.height = (maxY - minY) * WorldConstants.WORLD_CELL_SIZE
                it.color = Color(0xFFFFFF55.toInt())
            }

        var list: ArrayList<Int>? = null
        forEachBlock(minX, minY, maxX, maxY, idFilter) {
            if (DEBUG_INTERSECTION) {
                val block = blocks.get(it)!!
                Game.graphics2d.submit<DirectRenderable2D> {
                    it.layer = 80
                    it.texture = "blank"
                    it.x = block.minX * WorldConstants.WORLD_CELL_SIZE
                    it.y = block.minY * WorldConstants.WORLD_CELL_SIZE
                    it.width = (block.maxX - block.minX) * WorldConstants.WORLD_CELL_SIZE
                    it.height = (block.maxY - block.minY) * WorldConstants.WORLD_CELL_SIZE
                    it.color = Color(0xFF999955.toInt())
                }
            }

            if (list == null)
                list = arrayListOf()
            list!! += it
        }
        return list ?: emptyList()
    }

    fun forEachBlock(minX: Int, minY: Int, maxX: Int, maxY: Int, idFilter: (Int) -> Boolean = { true }, block: (Int) -> Unit) {
        fun intersectBlocks(minX0: Int, minY0: Int, maxX0: Int, maxY0: Int, minX1: Int, minY1: Int, maxX1: Int, maxY1: Int): Boolean {
            return maxX0 > minX1 && maxY0 > minY1 && minX0 < maxX1 && minY0 < maxY1
        }

        blocks.forEach {
            if (idFilter(it.key) && intersectBlocks(minX, minY, maxX, maxY, it.value.minX, it.value.minY, it.value.maxX, it.value.maxY))
                block(it.key)
        }
    }

    fun removeBlock(id: Int) {
        if (blocks.remove(id) != null)
            freeIds.push(id)
    }
}