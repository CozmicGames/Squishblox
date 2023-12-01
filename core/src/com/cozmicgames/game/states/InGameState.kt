package com.cozmicgames.game.states

import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.gui.GUI

abstract class InGameState : GameState {
    internal var returnState: GameState = this

    abstract val presentSource: String

    val gui: GUI = Game.guis.create()

    init {
        gui.pauseRenderingOnDisabled = true
    }

    protected open fun update(delta: Float) {}

    override fun render(delta: Float): () -> GameState {
        update(delta)

        return { returnState }
    }

    override fun end() {
        Game.guis.remove(gui)
    }
}