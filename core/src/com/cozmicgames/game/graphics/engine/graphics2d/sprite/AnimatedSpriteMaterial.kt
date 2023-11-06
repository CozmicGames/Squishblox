package com.cozmicgames.game.graphics.engine.graphics2d.sprite

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.g2d.Animation
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.shaders.Shader2D
import com.cozmicgames.game.textures
import com.cozmicgames.game.utils.float
import com.cozmicgames.game.utils.stringArray

class AnimatedSpriteMaterial : SpriteMaterial() {
    var textures: Array<String>
        get() = texturesInternal
        set(value) {
            isDirty = true
            texturesInternal = value
        }

    var keyFrameDuration
        get() = keyFrameDurationInternal
        set(value) {
            isDirty = true
            keyFrameDurationInternal = value
        }

    private var texturesInternal by stringArray { arrayOf("blank") }
    private var keyFrameDurationInternal by float { 0.0f }

    private var isDirty = true

    val animation: Animation<String>
        get() {
            if (isDirty) {
                updateAnimation()
                isDirty = false
            }

            return internalAnimation
        }

    lateinit var internalAnimation: Animation<String>

    private fun updateAnimation() {
        internalAnimation = Animation(keyFrameDuration, *textures)
    }

    fun setUniforms(shader: Shader2D, animationStateTime: Float) {
        setUniforms(shader)

        val textureName = animation.getKeyFrame(animationStateTime)
        val normalTextureName = textureName.substring(0, textureName.lastIndexOf(".")) + "_normals" + textureName.substring(textureName.lastIndexOf(".") + 1, textureName.length)

        val diffuseTexture = Game.textures.getTexture(textureName)
        val normalTexture = Game.textures.getTexture(normalTextureName) { "textures/default_normal_map.png" }

        shader.setUniform("u_diffuseTextureBounds", diffuseTexture.u, diffuseTexture.v, diffuseTexture.u2 - diffuseTexture.u, diffuseTexture.v2 - diffuseTexture.v)
        shader.setUniform("u_normalTextureBounds", normalTexture.u, normalTexture.v, normalTexture.u2 - normalTexture.u, normalTexture.v2 - normalTexture.v)
        shader.setUniform("u_normalTexture", 1)
        normalTexture.texture.bind(1)
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
    }
}