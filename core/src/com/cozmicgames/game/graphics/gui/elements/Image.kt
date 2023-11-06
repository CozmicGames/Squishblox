package com.cozmicgames.game.graphics.gui.elements

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.graphics2d.DirectRenderable2D
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.graphics.gui.GUIElement

open class Image(var texture: String, var color: Color = Color.WHITE, var flipX: Boolean = false, var flipY: Boolean = false) : GUIElement() {
    override fun render() {
        Game.graphics2d.submit<DirectRenderable2D> {
            it.layer = layer
            it.x = x
            it.y = y
            it.width = width
            it.height = height
            it.color = color
            it.texture = texture
            it.flipX = flipX
            it.flipY = flipY
        }
    }
}