package com.cozmicgames.game.graphics.renderer

import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction
import com.cozmicgames.game.guis

class InGameMenuRenderFunction : RenderFunction() {
    override fun render(delta: Float) {
        Game.guis.render()
    }
}