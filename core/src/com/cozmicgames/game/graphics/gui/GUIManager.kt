package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.utils.Updatable
import com.cozmicgames.game.graphics.gui.elements.Label
import com.cozmicgames.game.graphics.gui.skin.ColorDrawableValue
import com.cozmicgames.game.graphics.gui.skin.ColorValue
import com.cozmicgames.game.graphics.gui.skin.FontValue

class GUIManager : Updatable, Disposable {
    private val guis = arrayListOf<GUI>()
    private val workingGuis = arrayListOf<GUI>()
    private val windowGui = create()

    init {
        windowGui.baseLayer = 1000
    }

    fun create(): GUI {
        val gui = GUI()
        add(gui)
        return gui
    }

    fun remove(gui: GUI) {
        guis -= gui
    }

    fun add(gui: GUI) {
        guis += gui
    }

    fun isInputPositionVisible(): Boolean {
        guis.forEach {
            if (!it.isInputPositionVisible)
                return false
        }
        return true
    }

    //TODO: Implement windows where you set the constraints instead of a fixed size
    fun openWindow(title: String, width: Float, height: Float, isResizable: Boolean = true, hasTitleBar: Boolean = true, isScrollable: Boolean = true) = openWindow(title, (Gdx.graphics.width - width) * 0.5f, (Gdx.graphics.height - height) * 0.5f, width, height, isResizable, hasTitleBar, isScrollable)

    fun openWindow(title: String, x: Float, y: Float, width: Float, height: Float, isResizable: Boolean = true, hasTitleBar: Boolean = true, isScrollable: Boolean = true): GUIWindow {
        val window = GUIWindow(title, isResizable, hasTitleBar, isScrollable, windowGui.root.children.size * 100).also { //TODO: Temporary fix, actually fix multiple windows overlapping eventually
            it.windowX = x
            it.windowY = y
            it.windowWidth = width
            it.windowHeight = height
        }
        windowGui.addElement(window)
        return window
    }

    fun closeWindow(window: GUIWindow) {
        windowGui.removeElement(window)
        window.onClose()
    }

    override fun update(delta: Float) {
        workingGuis.clear()
        workingGuis.addAll(guis)
        workingGuis.forEach {
            it.update(delta)
        }
    }

    fun render() {
        guis.forEach {
            if (it !== windowGui)
                it.render()
        }
        windowGui.render()
    }

    override fun dispose() {
        guis.forEach {
            it.dispose()
        }
    }
}
