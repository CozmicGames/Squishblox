package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.math.Rectangle
import com.cozmicgames.game.graphics.RenderLayers

abstract class GUIElement {
    interface Listener {
        fun onEnter(element: GUIElement) {}
        fun onExit(element: GUIElement) {}
        fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {}
        fun onEnable(element: GUIElement) {}
        fun onDisable(element: GUIElement) {}
    }

    val constraints = GUIConstraints()

    val transform = GUITransform(this)

    val x by transform::x

    val y by transform::y

    val width by transform::width

    val height by transform::height

    var parent: GUIElement? = null
        set(value) {
            if (field == value)
                return

            field?.children?.remove(this)

            if (value != null)
                value.children += this

            gui = value?.gui

            field = value
        }

    var isEnabled = true
        set(value) {
            if (field == value)
                return

            field = value
            listeners.forEach {
                if (value)
                    it.onEnable(this)
                else
                    it.onDisable(this)
            }
        }

    var isHovered = false
        private set

    open val usedLayers = 1

    open val layer: Int get() = parent?.let { it.layer + it.usedLayers + additionalLayers } ?: RenderLayers.GUI_BASE_LAYER

    open val additionalLayers = 0

    var isSolid = true

    var minContentWidth = 0.0f
        protected set

    var minContentHeight = 0.0f
        protected set

    open val scissorRectangle: Rectangle? = null

    internal open var gui: GUI? = null
        get() = field ?: parent?.gui

    internal val children = arrayListOf<GUIElement>()

    private val listeners = arrayListOf<Listener>()
    private var wasHovered = false

    fun addElement(element: GUIElement) {
        element.parent = this
        element.onAdded()
    }

    fun removeElement(element: GUIElement) {
        element.parent = null
        element.onRemoved()
    }

    fun addListener(listener: Listener) {
        listeners += listener
    }

    fun removeListener(listener: Listener) {
        listeners -= listener
    }

    fun onUpdate(delta: Float, scissorRectangle: Rectangle?) {
        val gui = this.gui ?: return

        listeners.forEach {
            it.onUpdate(this, delta, scissorRectangle)
        }

        transform.update(delta)

        isHovered = gui.isHovered(x, y, width, height, layer, scissorRectangle)

        if (isHovered && !wasHovered)
            listeners.forEach {
                it.onEnter(this)
            }

        if (!isHovered && wasHovered)
            listeners.forEach {
                it.onExit(this)
            }

        wasHovered = isHovered
    }

    open fun submitElementsToUpdate() {
        gui?.submitElementToUpdate(this)

        children.forEach {
            it.submitElementsToUpdate()
        }
    }

    fun isHovered(checkLayer: Boolean = true) = gui?.isHovered(x, y, width, height, if (checkLayer) layer else null, scissorRectangle) == true

    open fun onAdded() {}
    open fun onRemoved() {}
    abstract fun render()
}