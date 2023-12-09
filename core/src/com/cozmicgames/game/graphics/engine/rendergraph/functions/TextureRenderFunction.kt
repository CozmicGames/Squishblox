package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction

class TextureRenderFunction(private val texture: Texture) : RenderFunction(), Disposable {
    private val batch = SpriteBatch()

    override fun render(delta: Float) {
        ScreenUtils.clear(Color.CLEAR)
        batch.begin()
        batch.draw(texture, 0.0f, 0.0f, pass.width.toFloat(), pass.height.toFloat())
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
    }
}