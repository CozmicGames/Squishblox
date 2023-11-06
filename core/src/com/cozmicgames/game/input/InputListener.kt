package com.cozmicgames.game.input

interface InputListener {
    fun onKey(key: Int, down: Boolean) {}
    fun onMouseButton(button: Int, down: Boolean) {}
    fun onTouch(x: Int, y: Int, pointer: Int, down: Boolean) {}
    fun onScroll(x: Float, y: Float) {}
    fun onChar(char: Char) {}
}