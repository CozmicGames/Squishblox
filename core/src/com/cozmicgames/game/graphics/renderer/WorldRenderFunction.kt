package com.cozmicgames.game.graphics.renderer

import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction
import com.cozmicgames.game.player

class WorldRenderFunction : RenderFunction() {
    private val renderer = WorldRenderer()

    override fun render(delta: Float) {
        renderer.render(delta, Game.player.camera, Game.player.scene)
        Game.player.currentGui?.render()
    }
}