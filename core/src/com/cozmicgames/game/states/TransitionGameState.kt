package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Interpolation
import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.graphics.renderer.ScreenshotRenderFunction
import com.cozmicgames.game.graphics.renderer.TransitionRenderFunction
import com.cozmicgames.game.renderGraph
import com.cozmicgames.game.renderer2d
import com.cozmicgames.game.time

class TransitionGameState(private val toGameState: WorldState, transition: Transition, val duration: Float = 0.66f, val interpolation: Interpolation = Interpolation.smooth) : GameState {
    private var time = 0.0f

    init {
        Gdx.app.postRunnable {
            Game.renderGraph.getNode(Renderer2D.TRANSITION_FROM)?.renderFunction = ScreenshotRenderFunction()
            Game.renderer2d.setPresentSource(Renderer2D.WORLD)
            toGameState.render(0.0f)
            Game.renderGraph.getNode(Renderer2D.TRANSITION_TO)?.renderFunction = ScreenshotRenderFunction()
            toGameState.gui.isEnabled = false

            Game.renderGraph.getNode(Renderer2D.TRANSITION)?.renderFunction = TransitionRenderFunction(transition)

            Game.renderer2d.setPresentSource(Renderer2D.TRANSITION)
        }
    }

    override fun render(delta: Float): () -> GameState {
        time += delta

        (Game.renderGraph.getNode(Renderer2D.TRANSITION)?.renderFunction as? TransitionRenderFunction)?.progress = interpolation.apply(time / duration)

        Game.renderGraph.render(Game.time.delta)

        if (time >= duration) {
            Gdx.app.postRunnable {
                toGameState.gui.isInteractionEnabled = true
                toGameState.gui.isEnabled = true
                Game.renderer2d.setPresentSource(Renderer2D.WORLD)
            }
            return { toGameState }
        } else
            return { this }
    }
}