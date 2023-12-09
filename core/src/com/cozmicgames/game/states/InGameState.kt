package com.cozmicgames.game.states

import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.gui.GUI
import com.cozmicgames.game.player
import com.cozmicgames.game.renderer2d

abstract class InGameState : GameState {
    internal var returnState: GameState = this

    abstract val presentSource: String

    val gui = GUI()

    private var isFirstRender = true

    open fun update(delta: Float) {}

    override fun render(delta: Float): () -> GameState {
        if (isFirstRender) {
            Game.player.currentState = this
            Game.renderer2d.setPresentSource(presentSource)
            isFirstRender = false
        }

        gui.update(delta)
        update(delta)

        return { returnState }
    }

    override fun end() {
        gui.dispose()
    }
}