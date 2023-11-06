package com.cozmicgames.game.graphics.gui.elements

import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.optionalDrawable

sealed class List<T : GUIElement>(val style: ListStyle = ListStyle()) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default") : this(skin.getStyle(ListStyle::class, name)!!)

    class ListStyle : GUIElementStyle() {
        var background by optionalDrawable { DefaultStyle.normalDrawable() }
        var backgroundDisabled by optionalDrawable { DefaultStyle.disabledDrawable() }
    }

    protected inner class Node(var index: Int) : GUIElement() {
        var element: T? = null
            set(value) {
                if (value == null && field != null)
                    removeElement(field!!)
                field = value
                if (value != null)
                    addElement(value)
            }

        init {
            isSolid = false
        }

        override fun render() {
        }
    }

    var size = 0
        protected set

    private var drawable = style.background

    init {
        addListener(object : Listener {
            override fun onEnable(element: GUIElement) {
                drawable = style.background
            }

            override fun onDisable(element: GUIElement) {
                drawable = style.backgroundDisabled
            }
        })
    }

    abstract fun add(element: T)

    abstract fun remove(index: Int)

    fun indexOf(element: T): Int {
        return children.indexOfFirst { it is List<*>.Node && it.element == element }
    }

    fun remove(element: T) {
        val index = indexOf(element)
        if (index >= 0)
            remove(index)
    }

    fun get(index: Int): T? {
        return (children.find { it is List<*>.Node && it.index == index } as? List<T>.Node)?.element
    }

    fun clear() {
        children.clear()
        size = 0
    }

    override fun render() {
        drawable?.let { it.drawable.draw(layer, it.color, x, y, width, height) }
    }
}