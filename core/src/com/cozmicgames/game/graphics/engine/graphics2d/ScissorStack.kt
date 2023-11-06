package com.cozmicgames.game.graphics.engine.graphics2d

import com.badlogic.gdx.math.Rectangle
import com.cozmicgames.game.utils.collections.DynamicStack
import kotlin.math.max
import kotlin.math.min

class ScissorStack {
    private val rectangles = DynamicStack<Rectangle>()

    val currentScissorRectangle get() = rectangles.current

    fun push(rectangle: Rectangle): Boolean {
        var rect = rectangle

        if (!rectangles.isEmpty) {
            val parent = rectangles.current!!
            val minX = max(parent.x, rectangle.x)
            val maxX = min(parent.x + parent.width, rectangle.x + rectangle.width)
            if (maxX - minX < 1.0f)
                return false

            val minY = max(parent.y, rectangle.y)
            val maxY = min(parent.y + parent.height, rectangle.y + rectangle.height)
            if (maxY - minY < 1.0f)
                return false

            rect = Rectangle()
            rect.x = minX
            rect.y = minY
            rect.width = maxX - minX
            rect.height = maxY - minY
        }

        rectangles.push(rect)
        return true
    }

    fun pop() {
        rectangles.pop()
    }
}