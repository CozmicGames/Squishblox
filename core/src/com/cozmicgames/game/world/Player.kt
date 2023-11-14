package com.cozmicgames.game.world

import com.badlogic.gdx.Gdx
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.utils.Updatable
import com.cozmicgames.game.utils.extensions.unproject
import com.cozmicgames.game.utils.maths.intersectPointRect

class Player: Updatable {
    val camera = PlayerCamera()

    var inputX = -Float.MAX_VALUE
        private set

    var inputY = -Float.MAX_VALUE
        private set

    var playState = PlayState.EDIT

    fun isHovered(minX: Float, minY: Float, maxX: Float, maxY: Float): Boolean {
        return intersectPointRect(inputX, inputY, minX, minY, maxX, maxY)
    }

    override fun update(delta: Float) {
        camera.update()

        camera.camera.unproject(Game.input.x - Gdx.graphics.safeInsetLeft, Game.input.y - Gdx.graphics.safeInsetBottom) { x, y, _ ->
            inputX = x
            inputY = y
        }
    }
}