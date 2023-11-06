package com.cozmicgames.game.utils

class Time : Updatable {
    var delta = 0.0f
    var sinceStart = 0.0f

    override fun update(delta: Float) {
        this.delta = delta
        sinceStart += delta
    }
}