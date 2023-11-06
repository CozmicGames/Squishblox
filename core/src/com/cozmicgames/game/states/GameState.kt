package com.cozmicgames.game.states

interface GameState {
    fun resize(width: Int, height: Int) {}
    fun render(delta: Float): () -> GameState
    fun end() {}
}
