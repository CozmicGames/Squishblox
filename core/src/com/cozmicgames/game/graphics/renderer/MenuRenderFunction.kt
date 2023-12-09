package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction
import com.cozmicgames.game.player

class MenuRenderFunction : RenderFunction() {
    private val skyColor = Color(0x6FC0F2FF)

    override fun render(delta: Float) {
        ScreenUtils.clear(skyColor)
        Game.player.currentGui?.render()
    }
}