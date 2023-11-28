package com.cozmicgames.game.player

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.states.MenuState
import com.cozmicgames.game.states.WorldState
import com.cozmicgames.game.utils.Updatable
import com.cozmicgames.game.utils.extensions.unproject
import com.cozmicgames.game.utils.maths.intersectPointRect
import com.cozmicgames.game.world.WorldConstants
import com.cozmicgames.game.world.WorldScene

class Player : Updatable {
    val camera = PlayerCamera()

    var inputX = -Float.MAX_VALUE
        private set

    var inputY = -Float.MAX_VALUE
        private set

    var playState = PlayState.EDIT

    val scene = WorldScene()

    lateinit var currentState: WorldState

    private var currentLevelData = ""
    private val playerPosition = Vector2()
    private val cameraFollower = CameraFollower(camera, playerPosition, 0.8f)

    fun isHovered(minX: Float, minY: Float, maxX: Float, maxY: Float): Boolean {
        return intersectPointRect(inputX, inputY, minX, minY, maxX, maxY)
    }

    override fun update(delta: Float) {
        if (playState != PlayState.EDIT) {
            playerPosition.x = scene.playerBlock?.let { it.minX + it.width * 0.5f } ?: 0.0f
            playerPosition.y = scene.playerBlock?.let { it.minY + it.height * 0.5f } ?: 0.0f
            cameraFollower.update()
        } else {
            camera.zoom *= 1.0f + (Game.input.scrollY * 0.1f)

            if (Game.input.isButtonDown(1)) {
                camera.position.x -= Game.input.deltaX * camera.zoom
                camera.position.y += Game.input.deltaY * camera.zoom
            }
        }

        camera.update()
        camera.camera.unproject(Game.input.x - Gdx.graphics.safeInsetLeft, Game.input.y - Gdx.graphics.safeInsetBottom) { x, y, _ ->
            inputX = x
            inputY = y
        }

        scene.playerBlock?.let {
            if (it.minY <= WorldConstants.WORLD_WATER_Y)
                resetLevel()
        }
    }

    fun startLevel(data: String) {
        currentLevelData = data
        scene.initialize(data)
    }

    fun resetLevel() {
        scene.initialize(currentLevelData)
    }

    fun onCompleteLevel() {
        scene.clearGameObjects()
        currentState.returnState = MenuState()
    }
}