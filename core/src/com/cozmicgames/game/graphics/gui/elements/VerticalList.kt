package com.cozmicgames.game.graphics.gui.elements

import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.absolute
import com.cozmicgames.game.graphics.gui.same
import com.cozmicgames.game.graphics.gui.skin.GUISkin

class VerticalList<T : GUIElement>(style: ListStyle = ListStyle()) : List<T>(style) {
    constructor(skin: GUISkin, name: String = "default") : this(skin.getStyle(ListStyle::class, name)!!)

    override fun add(element: T) {
        val previousNode = children.find { it is List<*>.Node && it.index == size - 1 }
        val node = Node(size)
        size++
        node.constraints.x = same()
        node.constraints.y = previousNode?.let { absolute { it.y + it.height - y } } ?: same()
        node.constraints.width = same(element)
        node.constraints.height = same(element)
        node.element = element
        addElement(node)
    }

    override fun remove(index: Int) {
        if (index < 0 || index >= size)
            return

        if (!children.removeIf { it is List<*>.Node && it.index == index })
            return

        for (i in (index + 1 until size)) {
            val node = children.find { it is List<*>.Node && it.index == i } as? List<*>.Node ?: continue
            node.index--
        }

        size--

        val nextNode = children.find { it is List<*>.Node && it.index == index } ?: return
        val previousNode = children.find { it is List<*>.Node && it.index == index - 1 }

        nextNode.constraints.y = previousNode?.let { absolute { it.y + it.height - y } } ?: same()
    }
}