package com.cozmicgames.game.states

import com.cozmicgames.game.Game
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState

class PlayState(levelData: String) : InGameState() {
    init {
        Game.player.startLevel(levelData)
        Game.player.playState = PlayState.PLAY
        Game.player.isPaused = false
    }

    override fun update(delta: Float) {

    }
}