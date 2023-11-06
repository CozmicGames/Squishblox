package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction
import com.cozmicgames.game.guis

class MenuRenderFunction : RenderFunction() {
    override fun render(delta: Float) {
        ScreenUtils.clear(Color.CLEAR)
        Game.guis.render()
    }
}