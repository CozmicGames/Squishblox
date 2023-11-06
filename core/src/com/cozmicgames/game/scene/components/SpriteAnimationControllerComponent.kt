package com.cozmicgames.game.scene.components

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.cozmicgames.game.utils.Updatable
import com.cozmicgames.game.graphics.engine.graphics2d.sprite.AnimatedSpriteMaterial
import com.cozmicgames.game.scene.Component

class SpriteAnimationControllerComponent : Updatable, Component() {
    var animationStateTime = 0.0f
        private set

    var playMode = PlayMode.NORMAL
        set(value) {
            field = value
            material?.animation?.playMode = value
        }

    var isPlaying = false

    private val material get() = gameObject.getComponent<SpriteComponent>()?.material as? AnimatedSpriteMaterial

    fun play(playMode: PlayMode = this.playMode) {
        this.playMode = playMode
        isPlaying = true

        material?.animation?.playMode = playMode
    }

    fun stop() {
        isPlaying = false
    }

    fun reset() {
        animationStateTime = 0.0f
    }

    override fun update(delta: Float) {
        if (isPlaying)
            animationStateTime += delta
    }
}