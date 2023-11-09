package com.cozmicgames.game.graphics.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.rendergraph.RenderFunction
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.player

class InGameRenderFunction : RenderFunction() {
    override fun render(delta: Float) {
        ScreenUtils.clear(Color.SKY)
        Game.graphics2d.render(Game.player.camera.camera) { it in RenderLayers.WORLD_LAYER_BEGIN..RenderLayers.WORLD_LAYER_END }
    }
}