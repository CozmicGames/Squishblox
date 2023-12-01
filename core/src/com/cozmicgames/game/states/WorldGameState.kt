package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.common.utils.extensions.safeWidth
import com.cozmicgames.game.world.WorldConstants

abstract class WorldGameState : InGameState(), SuspendableGameState {
    override val presentSource = Renderer2D.WORLD

    init {
        Game.player.currentState = this
        Game.player.camera.getMaxZoom = { 1.5f }
        Game.player.camera.getMinZoom = { 0.5f }
        Game.player.camera.getMinY = { WorldConstants.WORLD_MIN_Y }
        Game.player.camera.getMinX = { WorldConstants.WORLD_MIN_X + Gdx.graphics.safeWidth * 0.5f }
        Game.player.camera.getMaxX = { WorldConstants.WORLD_MAX_X - Gdx.graphics.safeWidth * 0.5f }
    }

    override fun update(delta: Float) {
        Game.player.scene.update(delta)
    }

    override fun suspend() {
        gui.isInteractionEnabled = false
    }

    override fun resumeFromSuspension() {
        Game.renderer2d.setPresentSource(presentSource)

        gui.isInteractionEnabled = true
    }

    override fun end() {
        Game.guis.remove(gui)
    }
}