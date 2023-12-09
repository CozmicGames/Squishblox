package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.Game
import com.cozmicgames.common.utils.extensions.safeHeight
import com.cozmicgames.common.utils.extensions.safeWidth
import com.cozmicgames.game.graphics.engine.graphics2d.ScissorStack
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.input
import com.cozmicgames.common.utils.extensions.unproject
import com.cozmicgames.common.utils.maths.intersectPointRect
import kotlin.math.abs

class GUI(private val baseWindowLayer: Int = 1000, private val maxLayersInWindow: Int = 100) : Disposable {
    var inputX = -Float.MAX_VALUE
        private set

    var inputY = -Float.MAX_VALUE
        private set

    var scrollX = 0.0f
        private set

    var scrollY = 0.0f
        private set

    var textSize = 15.0f

    var scrollSpeed = 1.0f

    val camera = OrthographicCamera()

    val root = object : GUIElement() {
        override val usedLayers get() = baseLayer

        init {
            isSolid = false
        }

        override fun render() {}
    }

    var isInteractionEnabled = true
        set(value) {
            if (!value) {
                inputX = -Float.MAX_VALUE
                inputY = -Float.MAX_VALUE
                scrollX = 0.0f
                scrollY = 0.0f
            }

            field = value
        }

    var isInputPositionVisible = true
        private set

    var isEnabled by root::isEnabled

    var baseLayer = 0

    private val layers = hashMapOf<Int, GUILayer>()
    private val workingLayers = arrayListOf<GUILayer>()
    private val usedLayers = hashSetOf<Int>()
    private val scissorStack = ScissorStack()
    private var deltaCounter = 0.0f
    private val windows = arrayListOf<GUIWindow>()

    init {
        root.constraints.x = absolute(0.0f)
        root.constraints.y = absolute(0.0f)
        root.constraints.width = fill()
        root.constraints.height = fill()
        root.gui = this
    }

    fun addElement(element: GUIElement) {
        root.addElement(element)
    }

    fun removeElement(element: GUIElement): Boolean {
        return root.removeElement(element)
    }

    fun openWindow(title: String, width: Float, height: Float, isResizable: Boolean = true, hasTitleBar: Boolean = true, isScrollable: Boolean = true) = openWindow(title, (Gdx.graphics.width - width) * 0.5f, (Gdx.graphics.height - height) * 0.5f, width, height, isResizable, hasTitleBar, isScrollable)

    fun openWindow(title: String, x: Float, y: Float, width: Float, height: Float, isResizable: Boolean = true, hasTitleBar: Boolean = true, isScrollable: Boolean = true): GUIWindow {
        val window = GUIWindow(title, isResizable, hasTitleBar, isScrollable, baseWindowLayer + windows.size * maxLayersInWindow).also { //TODO: Temporary fix, actually fix multiple windows overlapping eventually
            it.windowX = x
            it.windowY = y
            it.windowWidth = width
            it.windowHeight = height
        }
        windows += window
        addElement(window)
        return window
    }

    fun closeWindow(window: GUIWindow) {
        if (removeElement(window)) {
            windows -= window
            window.onClose()
        }
    }

    fun pushScissor(rectangle: Rectangle) {
        scissorStack.push(rectangle)
    }

    fun popScissor() {
        scissorStack.pop()
    }

    fun submitElementToUpdate(element: GUIElement) {
        layers.getOrPut(element.layer) { GUILayer(element.layer) }.addElement(element, scissorStack.currentScissorRectangle)
    }

    fun isHovered(x: Float, y: Float, width: Float, height: Float, checkingLayer: Int?, scissorRectangle: Rectangle?): Boolean {
        fun isPositionVisible(x: Float, y: Float, checkingLayer: Int): Boolean {
            for (layer in workingLayers) {
                if (layer.layerIndex == checkingLayer)
                    break

                if (!layer.isVisible(x, y))
                    return false
            }

            return true
        }

        if (!isInteractionEnabled)
            return false

        if (scissorRectangle?.contains(inputX, inputY) == false)
            return false

        checkingLayer?.let {
            if (!isPositionVisible(inputX, inputY, it))
                return false
        }

        return intersectPointRect(inputX, inputY, x, y, x + width, y + height)
    }

    fun update(delta: Float) {
        camera.setToOrtho(true, Gdx.graphics.safeWidth.toFloat(), Gdx.graphics.safeHeight.toFloat())

        if (isInteractionEnabled) {
            scrollX -= Game.input.scrollX * scrollSpeed
            scrollY -= Game.input.scrollY * scrollSpeed
        }

        camera.unproject(Game.input.x - Gdx.graphics.safeInsetLeft, Game.input.y - Gdx.graphics.safeInsetBottom) { x, y, _ ->
            inputX = x
            inputY = y
        }

        root.submitElementsToUpdate()

        workingLayers.clear()
        workingLayers.addAll(layers.values)
        workingLayers.sortByDescending { it.layerIndex }
        workingLayers.forEach {
            it.update(delta)
        }
        isInputPositionVisible = true
        for (layer in workingLayers) {
            if (!layer.isVisible(inputX, inputY)) {
                isInputPositionVisible = false
                break
            }
        }
        workingLayers.forEach {
            it.reset()
        }

        deltaCounter += delta //TODO: Smoother scrolling, this is pretty clunky
        while (deltaCounter > 1.0f / 60.0f) {
            scrollX *= 0.5f
            scrollY *= 0.5f
            deltaCounter -= 1.0f / 60.0f
        }

        if (abs(scrollX) < 0.1f)
            scrollX = 0.0f

        if (abs(scrollY) < 0.1f)
            scrollY = 0.0f
    }

    fun render() {
        usedLayers.clear()

        fun renderElement(element: GUIElement) {
            repeat(element.usedLayers) {
                usedLayers += element.layer + it
            }

            element.render()

            element.scissorRectangle?.let {
                Game.graphics2d.pushScissor(it)
            }

            element.children.forEach {
                renderElement(it)
            }

            element.scissorRectangle?.let {
                Game.graphics2d.popScissor()
            }
        }

        renderElement(root)

        Game.graphics2d.render(camera) { it in usedLayers }
    }

    override fun dispose() {
        fun disposeElement(element: GUIElement) {
            (element as? Disposable)?.dispose()
            element.children.forEach {
                disposeElement(it)
            }
        }

        disposeElement(root)
    }
}