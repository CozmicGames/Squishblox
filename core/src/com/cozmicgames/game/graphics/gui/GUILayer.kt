package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.math.Rectangle
import com.cozmicgames.game.utils.collections.Pool

class GUILayer(val layerIndex: Int) {
    private class Entry {
        lateinit var element: GUIElement
        val scissorRectangle = Rectangle()
        var isScissored = false
    }

    private val entryPool = Pool(supplier = { Entry() })
    private val entries = arrayListOf<Entry>()
    private val visibility = GUIVisibility()

    fun addElement(element: GUIElement, scissorRectangle: Rectangle?) {
        val entry = entryPool.obtain()
        entry.element = element

        if (scissorRectangle != null) {
            entry.scissorRectangle.set(scissorRectangle)
            entry.isScissored = true
        } else
            entry.isScissored = false

        entries += entry

        if (!element.isSolid)
            return

        visibility.add(element.x, element.y, element.width, element.height, scissorRectangle)
    }

    fun update(delta: Float) {
        entries.forEach {
            it.element.onUpdate(delta, if (it.isScissored) it.scissorRectangle else null)
            entryPool.free(it)
        }
        entries.clear()
    }

    fun isVisible(x: Float, y: Float): Boolean {
        return !visibility.contains(x, y)
    }

    fun reset() {
        visibility.reset()
    }
}