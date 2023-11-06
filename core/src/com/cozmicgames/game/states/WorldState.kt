package com.cozmicgames.game.states

import com.badlogic.gdx.Input
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.GUI
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.guis
import com.cozmicgames.game.input
import com.cozmicgames.game.renderGraph
import com.cozmicgames.game.renderer2d
import com.cozmicgames.game.time
import com.cozmicgames.game.world.WorldScene

class WorldState : SuspendableGameState {
    internal var returnState: GameState = this

    val gui: GUI = Game.guis.create()
    val scene = WorldScene()

    init {
        gui.isInteractionEnabled = false
        gui.pauseRenderingOnDisabled = true
    }

    override fun render(delta: Float): () -> GameState {
        if (gui.isInteractionEnabled && Game.input.isKeyJustDown(Input.Keys.ESCAPE))
            returnState = InGameMenuState(this)

        scene.update(delta)
        Game.renderGraph.render(Game.time.delta)

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