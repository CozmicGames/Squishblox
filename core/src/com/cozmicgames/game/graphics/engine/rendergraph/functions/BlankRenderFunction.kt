package com.cozmicgames.game.graphics.engine.rendergraph.functions

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction

class BlankRenderFunction(private val color: Color = Color.CLEAR) : RenderFunction() {
    override fun render(delta: Float) {
        ScreenUtils.clear(color)
    }
}