package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.gui.GUI
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.utils.extensions.safeWidth
import com.cozmicgames.game.world.PlayState
import com.cozmicgames.game.world.WorldConstants
import com.cozmicgames.game.world.WorldScene

class WorldState : SuspendableGameState {
    internal var returnState: GameState = this

    val gui: GUI = Game.guis.create()
    val scene = WorldScene()

    init {
        gui.isInteractionEnabled = false
        gui.pauseRenderingOnDisabled = true

        Game.player.camera.getMaxZoom = { 1.5f }
        Game.player.camera.getMinZoom = { 0.5f }
        Game.player.camera.getMinY = { WorldConstants.WORLD_MIN_Y }
        Game.player.camera.getMinX = { WorldConstants.WORLD_MIN_X + Gdx.graphics.safeWidth * 0.5f }
        Game.player.camera.getMaxX = { WorldConstants.WORLD_MAX_X - Gdx.graphics.safeWidth * 0.5f }
    }

    override fun render(delta: Float): () -> GameState {
        if (gui.isInteractionEnabled && Game.input.isKeyJustDown(Input.Keys.ESCAPE))
            returnState = InGameMenuState(this)

        scene.update(delta)
        Game.renderGraph.render(Game.time.delta)

        if (Game.input.isKeyJustDown(Input.Keys.NUM_1))
            scene.editState = WorldScene.EditState.CREATE
        if (Game.input.isKeyJustDown(Input.Keys.NUM_2))
            scene.editState = WorldScene.EditState.EDIT
        if (Game.input.isKeyJustDown(Input.Keys.NUM_3))
            scene.editState = WorldScene.EditState.DELETE
        if (Game.input.isKeyJustDown(Input.Keys.TAB)) {
            if (Game.player.playState == PlayState.EDIT)
                Game.player.playState = PlayState.PLAY
            else
                Game.player.playState = PlayState.EDIT
        }

        if (Game.input.isKeyJustDown(Input.Keys.ENTER))
            scene.spawnPlayer(5, 5)

        return { returnState }
    }

    override fun suspend() {
        gui.isInteractionEnabled = false
    }

    override fun resumeFromSuspension() {
        Game.renderer2d.setPresentSource(Renderer2D.WORLD)

        gui.isInteractionEnabled = true
    }

    override fun end() {
        Game.guis.remove(gui)
    }
}