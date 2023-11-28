package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.Gdx
import com.cozmicgames.common.utils.extensions.sumOf
import com.cozmicgames.common.utils.collections.Array2D
import kotlin.math.max

fun absolute(value: Float, mirror: Boolean = false) = absolute(mirror) { value }

fun absolute(mirror: Boolean = false, valueGetter: () -> Float) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.X -> if (mirror) ((parent?.x ?: 0.0f) + (parent?.width ?: Gdx.graphics.width.toFloat())) - (valueGetter() + element.width) else (parent?.x ?: 0.0f) + valueGetter()
        Type.Y -> if (mirror) ((parent?.y ?: 0.0f) + (parent?.height ?: Gdx.graphics.height.toFloat())) - (valueGetter() + element.height) else (parent?.y ?: 0.0f) + valueGetter()
        Type.WIDTH, Type.HEIGHT -> valueGetter()
    }
}

fun relative(value: Float, toElement: GUIElement? = null) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.X -> (toElement?.x ?: parent?.x ?: 0.0f) + (toElement?.width ?: parent?.width ?: Gdx.graphics.width.toFloat()) * value
        Type.Y -> (toElement?.y ?: parent?.y ?: 0.0f) + (toElement?.height ?: parent?.height ?: Gdx.graphics.height.toFloat()) * value
        Type.WIDTH -> (toElement?.width ?: parent?.width ?: Gdx.graphics.width.toFloat()) * value
        Type.HEIGHT -> (toElement?.height ?: parent?.height ?: Gdx.graphics.height.toFloat()) * value
    }
}

fun center() = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.X -> (parent?.x ?: 0.0f) + ((parent?.width ?: Gdx.graphics.width.toFloat()) - element.width) * 0.5f
        Type.Y -> (parent?.y ?: 0.0f) + ((parent?.height ?: Gdx.graphics.height.toFloat()) - element.height) * 0.5f
        else -> throw UnsupportedOperationException()
    }
}

fun aspect(value: Float = 1.0f) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.WIDTH -> element.height * value
        Type.HEIGHT -> element.width / value
        else -> throw UnsupportedOperationException()
    }
}

fun fill() = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.WIDTH -> (parent?.width ?: Gdx.graphics.width.toFloat())
        Type.HEIGHT -> (parent?.height ?: Gdx.graphics.height.toFloat())
        else -> throw UnsupportedOperationException()
    }
}

fun offset(toElement: GUIElement, value: Float) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.X -> (parent?.x ?: 0.0f) + (if (value >= 0.0f) toElement.x + toElement.width + value else toElement.x - value - toElement.width)
        Type.Y -> (parent?.y ?: 0.0f) + (if (value >= 0.0f) toElement.y + toElement.height + value else toElement.y - value - toElement.height)
        else -> throw UnsupportedOperationException()
    }
}

fun <T : GUIElement> distribute(index: Int, elements: Array<T>) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.X -> {
            val totalOccupiedSize = elements.sumOf { it.width }
            val spacing = ((parent?.width ?: Gdx.graphics.width.toFloat()) - totalOccupiedSize) / (elements.size - 1)
            var value = 0.0f
            if (index > 0)
                repeat(index) {
                    value += elements[it].width
                    value += spacing
                }
            (parent?.x ?: 0.0f) + value
        }

        Type.Y -> {
            val totalOccupiedSize = elements.sumOf { it.height }
            val spacing = ((parent?.height ?: Gdx.graphics.height.toFloat()) - totalOccupiedSize) / (elements.size - 1)
            var value = 0.0f
            if (index > 0)
                repeat(index) {
                    value += elements[it].height
                    value += spacing
                }
            (parent?.y ?: 0.0f) + value
        }

        else -> throw UnsupportedOperationException()
    }
}

fun evenly(spacing: Float) = evenly { spacing }

fun evenly(spacing: () -> Float = { 0.0f }) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.X ->
            (parent?.x ?: 0.0f) + if (parent == null)
                0.0f
            else {
                val width = (parent.width - (parent.children.size * spacing())) / parent.children.size
                parent.children.indexOf(element) * width
            }

        Type.Y -> (parent?.y ?: 0.0f) + if (parent == null)
            0.0f
        else {
            val height = (parent.height - (parent.children.size * spacing())) / parent.children.size
            parent.children.indexOf(element) * height
        }

        Type.WIDTH -> if (parent == null)
            throw UnsupportedOperationException()
        else
            (parent.width - (parent.children.size * spacing())) / parent.children.size

        Type.HEIGHT -> if (parent == null)
            throw UnsupportedOperationException()
        else
            (parent.height - (parent.children.size * spacing())) / parent.children.size
    }
}

fun same(asElement: GUIElement? = null) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.X -> asElement?.x ?: parent?.x ?: 0.0f
        Type.Y -> asElement?.y ?: parent?.y ?: 0.0f
        Type.WIDTH -> asElement?.width ?: parent?.width ?: Gdx.graphics.width.toFloat()
        Type.HEIGHT -> asElement?.height ?: parent?.height ?: Gdx.graphics.height.toFloat()
    }
}

fun packed() = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement) = when (type) {
        Type.WIDTH -> {
            val max = element.children.maxOfOrNull { it.x + it.width } ?: 0.0f
            val min = element.children.minOfOrNull { it.x } ?: 0.0f
            max(max - min, element.minContentWidth)
        }

        Type.HEIGHT -> {
            val max = element.children.maxOfOrNull { it.y + it.height } ?: 0.0f
            val min = element.children.minOfOrNull { it.y } ?: 0.0f
            max(max - min, element.minContentHeight)
        }

        else -> throw UnsupportedOperationException()
    }
}

fun add(a: GUIConstraint, b: GUIConstraint) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement): Float {
        a.type = type
        b.type = type
        return a.getValue(parent, element) + b.getValue(parent, element)
    }
}

fun subtract(a: GUIConstraint, b: GUIConstraint) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement): Float {
        a.type = type
        b.type = type
        return a.getValue(parent, element) - b.getValue(parent, element)
    }
}

fun multiply(a: GUIConstraint, b: GUIConstraint) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement): Float {
        a.type = type
        b.type = type
        return a.getValue(parent, element) * b.getValue(parent, element)
    }
}

fun divide(a: GUIConstraint, b: GUIConstraint) = object : GUIConstraint() {
    override fun getValue(parent: GUIElement?, element: GUIElement): Float {
        a.type = type
        b.type = type
        return a.getValue(parent, element) / b.getValue(parent, element)
    }
}

operator fun GUIConstraint.plus(other: GUIConstraint) = add(this, other)

operator fun GUIConstraint.minus(other: GUIConstraint) = subtract(this, other)

operator fun GUIConstraint.times(other: GUIConstraint) = multiply(this, other)

operator fun GUIConstraint.div(other: GUIConstraint) = divide(this, other)

class GUIConstraints {
    companion object {
        private val DEFAULT_X = absolute(0.0f).apply { type = GUIConstraint.Type.X }
        private val DEFAULT_Y = absolute(0.0f).apply { type = GUIConstraint.Type.Y }
        private val DEFAULT_WIDTH = fill().apply { type = GUIConstraint.Type.WIDTH }
        private val DEFAULT_HEIGHT = fill().apply { type = GUIConstraint.Type.HEIGHT }
    }

    var x: GUIConstraint = DEFAULT_X
        set(value) {
            value.type = GUIConstraint.Type.X
            field = value
        }

    var y: GUIConstraint = DEFAULT_Y
        set(value) {
            value.type = GUIConstraint.Type.Y
            field = value
        }

    var width: GUIConstraint = DEFAULT_WIDTH
        set(value) {
            value.type = GUIConstraint.Type.WIDTH
            field = value
        }

    var height: GUIConstraint = DEFAULT_HEIGHT
        set(value) {
            value.type = GUIConstraint.Type.HEIGHT
            field = value
        }
}

fun GUIConstraints.split(rows: Int, cols: Int): Array2D<GUIConstraints> {
    val result = Array2D(rows, cols) { _, _ -> GUIConstraints() }

    val rowHeight = 1.0f / rows
    val colWidth = 1.0f / cols

    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val rowConstraints = result[row, col]
            rowConstraints?.x = relative(col * colWidth)
            rowConstraints?.y = relative(row * rowHeight)
            rowConstraints?.width = relative(colWidth)
            rowConstraints?.height = relative(rowHeight)
        }
    }
    return result
}
