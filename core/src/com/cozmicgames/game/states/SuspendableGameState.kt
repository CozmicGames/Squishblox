package com.cozmicgames.game.states

interface SuspendableGameState: GameState {
    fun suspend()
    fun resumeFromSuspension()
}