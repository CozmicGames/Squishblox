package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.ScreenUtils
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.graphics.renderer.ScreenshotRenderFunction
import com.cozmicgames.game.graphics.renderer.TransitionRenderFunction

class TransitionGameState(private val toGameState: InGameState, transition: Transition, val duration: Float = 0.5f, val interpolation: Interpolation = Interpolation.smooth) : GameState {
    private var time = 0.0f

    init {
        Game.renderGraph.render(0.0f)
        Game.renderGraph.getNode(Renderer2D.TRANSITION_FROM)?.renderFunction = ScreenshotRenderFunction()

        Game.renderer2d.setPresentSource(toGameState.presentSource)
        toGameState.render(0.0f)
        Game.renderGraph.render(0.0f)
        Game.renderGraph.getNode(Renderer2D.TRANSITION_TO)?.renderFunction = ScreenshotRenderFunction()
        toGameState.gui.isEnabled = false
        Game.renderGraph.getNode(Renderer2D.TRANSITION)?.renderFunction = TransitionRenderFunction(transition)
        Game.renderer2d.setPresentSource(Renderer2D.TRANSITION)

        Game.renderer2d.setPresentSource(Renderer2D.TRANSITION)
    }

    override fun render(delta: Float): () -> GameState {
        time += delta

        (Game.renderGraph.getNode(Renderer2D.TRANSITION)?.renderFunction as? TransitionRenderFunction)?.progress = interpolation.apply(time / duration)

        if (time >= duration) {
            Gdx.app.postRunnable {
                toGameState.gui.isInteractionEnabled = true
                toGameState.gui.isEnabled = true
                Game.renderer2d.setPresentSource(toGameState.presentSource)
            }
            return { toGameState }
        } else
            return { this }
    }
}