package com.cozmicgames.game.utils.extensions

import com.badlogic.gdx.math.Rectangle

fun Rectangle.infinite() {
    x = -Float.MAX_VALUE * 0.5f
    y = -Float.MAX_VALUE * 0.5f
    width = Float.MAX_VALUE
    height = Float.MAX_VALUE
}
