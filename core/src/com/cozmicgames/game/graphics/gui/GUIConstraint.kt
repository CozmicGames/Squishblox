package com.cozmicgames.game.graphics.gui

abstract class GUIConstraint {
    internal enum class Type {
        X,
        Y,
        WIDTH,
        HEIGHT
    }

    internal lateinit var type: Type

    abstract fun getValue(parent: GUIElement?, element: GUIElement): Float
}
