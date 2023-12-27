package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder
import com.badlogic.gdx.math.Interpolation
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.engine.rendergraph.functions.BlitRenderFunction
import com.cozmicgames.game.graphics.renderer.Renderer2D
import com.cozmicgames.game.graphics.renderer.ScreenshotRenderFunction
import com.cozmicgames.game.graphics.renderer.TransitionRenderFunction

class TransitionGameState(private val toGameState: InGameState, transition: Transition, val duration: Float = 1.0f, val interpolation: Interpolation = Interpolation.fade) : GameState {
    private var time = 0.0f

    init {
        Game.renderGraph.getNode(Renderer2D.TRANSITION_FROM)?.renderFunction = ScreenshotRenderFunction()

        Gdx.app.postRunnable {
            Game.player.currentState = toGameState
            toGameState.gui.isInteractionEnabled = false
            toGameState.gui.update(0.0f)
            toGameState.update(0.0f)

            Game.renderGraph.getNode(Renderer2D.TRANSITION_TO)?.renderFunction = BlitRenderFunction(toGameState.presentSource, 0)
            Game.renderGraph.getNode(Renderer2D.TRANSITION)?.renderFunction = TransitionRenderFunction(transition)
            Game.renderer2d.setPresentSource(Renderer2D.TRANSITION)
        }
    }

    override fun render(delta: Float): () -> GameState {
        time += delta

        (Game.renderGraph.getNode(Renderer2D.TRANSITION)?.renderFunction as? TransitionRenderFunction)?.progress = interpolation.apply(time / duration)

        if (time >= duration) {
            Gdx.app.postRunnable {
                toGameState.gui.isInteractionEnabled = true
            }
            return { toGameState }
        } else
            return { this }
    }
}