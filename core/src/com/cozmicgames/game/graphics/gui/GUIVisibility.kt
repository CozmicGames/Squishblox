package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.cozmicgames.common.utils.collections.Pool
import com.cozmicgames.common.utils.collections.PriorityList
import kotlin.math.max
import kotlin.math.min

class GUIVisibility {
    class Node : Comparable<Node> {
        var x = 0.0f
        var y = 0.0f
        var width = 0.0f
        var height = 0.0f

        override fun compareTo(other: Node): Int {
            return if (x < other.x) -1 else if (x > other.x) 1 else 0
        }
    }

    private val nodePool = Pool(supplier = { Node() })
    private val nodesInternal = PriorityList<Node>()

    val nodes get() = nodesInternal.asIterable()

    private var minX = Float.MAX_VALUE
    private var minY = Float.MAX_VALUE
    private var maxX = -Float.MAX_VALUE
    private var maxY = -Float.MAX_VALUE

    fun add(x: Float, y: Float, width: Float, height: Float, scissorRectangle: Rectangle?) {
        val minX: Float
        val minY: Float
        val maxX: Float
        val maxY: Float

        if (scissorRectangle != null) {
            minX = max(x, scissorRectangle.x)
            maxX = min(x + width, scissorRectangle.x + scissorRectangle.width)
            if (maxX - minX < 1.0f)
                return

            minY = max(y, scissorRectangle.y)
            maxY = min(y + height, scissorRectangle.y + scissorRectangle.height)
            if (maxY - minY < 1.0f)
                return
        } else {
            minX = x
            minY = y
            maxX = x + width
            maxY = y + height
        }

        nodesInternal.add(nodePool.obtain().also {
            it.x = minX
            it.y = minY
            it.width = maxX - minX
            it.height = maxY - minY
        })

        if (minX < this.minX)
            this.minX = minX

        if (minY < this.minY)
            this.minY = minY

        if (maxX > this.maxX)
            this.maxX = maxX

        if (maxY > this.maxY)
            this.maxY = maxY
    }

    operator fun contains(point: Vector2) = contains(point.x, point.y)

    fun contains(x: Float, y: Float): Boolean {
        if (nodesInternal.isEmpty())
            return false

        if (x < minX || x > maxX || y < minY || y > maxY)
            return false

        for (node in nodesInternal) {
            if (x < node.x)
                return false

            if (node.x <= x && node.x + node.width >= x && node.y <= y && node.y + node.height >= y)
                return true
        }

        return false
    }

    fun reset() {
        nodesInternal.forEach(nodePool::free)
        nodesInternal.clear()
        minX = 0.0f
        minY = 0.0f
        maxX = 0.0f
        maxY = 0.0f
    }
}