package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction

class MenuRenderFunction : RenderFunction() {
    private val skyColor = Color(0x6FC0F2FF)

    override fun render(delta: Float) {
        ScreenUtils.clear(skyColor)
        Game.guis.render()
    }
}