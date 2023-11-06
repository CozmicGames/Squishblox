package com.cozmicgames.game.graphics.gui.elements

interface ToggleButton {
    var onClick: () -> Unit
    var isToggled: Boolean
}