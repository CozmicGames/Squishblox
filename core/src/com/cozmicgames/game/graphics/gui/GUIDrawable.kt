package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.graphics2d.BasicRenderable2D
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.textures
import com.cozmicgames.game.time

abstract class GUIDrawable {
    var paddingLeft = 0.0f
    var paddingRight = 0.0f
    var paddingTop = 0.0f
    var paddingBottom = 0.0f
    abstract fun draw(layer: Int, color: Color, x: Float, y: Float, width: Float, height: Float)
}

class TextureDrawable(private val texture: String, private val flipX: Boolean = false, private val flipY: Boolean = false) : GUIDrawable() {
    override fun draw(layer: Int, color: Color, x: Float, y: Float, width: Float, height: Float) {
        Game.graphics2d.submit<BasicRenderable2D> {
            it.layer = layer
            it.x = x
            it.y = y
            it.width = width
            it.height = height
            it.texture = texture
            it.color = color
            it.flipX = flipX
            it.flipY = flipY
        }
    }
}

class NinepatchDrawable(private val texture: String, private val left: Int, private val right: Int, private val top: Int, private val bottom: Int) : GUIDrawable() {
    private var ninePatch = NinePatch(Game.textures.getTexture(texture), left, right, top, bottom)
    private var textureVersion = Game.textures.getVersion(texture)

    override fun draw(layer: Int, color: Color, x: Float, y: Float, width: Float, height: Float) {
        val currentTextureVersion = Game.textures.getVersion(texture)
        if (currentTextureVersion != textureVersion) {
            ninePatch = NinePatch(Game.textures.getTexture(texture), left, right, top, bottom)
            textureVersion = currentTextureVersion
        }

        Game.graphics2d.submit<NinepatchRenderable2D> {
            it.layer = layer
            it.x = x
            it.y = y
            it.width = width
            it.height = height
            it.ninePatch = ninePatch
            it.color = color
        }
    }
}

class AnimatedDrawable(private val textures: Array<String>, private val keyFrameDuration: Float, private val flipX: Boolean = false, private val flipY: Boolean = false) : GUIDrawable() {
    private val animation = Animation(keyFrameDuration, *textures)

    override fun draw(layer: Int, color: Color, x: Float, y: Float, width: Float, height: Float) {
        val texture = animation.getKeyFrame(Game.time.sinceStart, true)

        Game.graphics2d.submit<BasicRenderable2D> {
            it.layer = layer
            it.x = x
            it.y = y
            it.width = width
            it.height = height
            it.texture = texture
            it.color = color
            it.flipX = flipX
            it.flipY = flipY
        }
    }
}
