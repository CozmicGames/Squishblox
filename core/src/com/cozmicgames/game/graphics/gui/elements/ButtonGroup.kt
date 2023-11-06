package com.cozmicgames.game.graphics.gui.elements

class ButtonGroup<T : ToggleButton> {
    private inner class Entry(val button: T) {
        val previousOnClick = button.onClick

        init {
            button.onClick = {
                buttons.forEach {
                    if (it.button !== button)
                        it.button.isToggled = false
                }
                button.isToggled = true
                selected = button
            }
        }
    }

    var selected: T? = null
        set(value) {
            if (value == null)
                field?.isToggled = false

            field = value
        }

    private val buttons = arrayListOf<Entry>()

    fun add(button: T) {
        buttons += Entry(button)
    }

    fun remove(button: T) {
        val entry = buttons.find { it.button === button } ?: return
        button.onClick = entry.previousOnClick
    }
}