package com.cozmicgames.game.graphics.engine.graphics2d.sprite

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.utils.boolean
import com.cozmicgames.game.utils.int
import com.cozmicgames.game.utils.string
import com.cozmicgames.game.graphics.engine.shaders.Shader2D

sealed class SpriteMaterial : Properties() {
    var flipX by boolean { false }
    var flipY by boolean { false }

    var color = Color()
        get() {
            Color.abgr8888ToColor(field, colorInternal)
            return field
        }
        set(value) {
            field = value
            colorInternal = value.toIntBits()
        }

    var shader by string { "shaders/sprite.shader2d" }
    var castsShadows by boolean { true }
    var receivesShadows by boolean { true }
    var isLit by boolean { true }

    private var colorInternal by int { Color.WHITE.toIntBits() }

    open fun setUniforms(shader: Shader2D) {
        shader.setUniform("u_isLit", isLit)
        shader.setUniform("u_receivesShadows", receivesShadows)
    }
}