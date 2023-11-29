package com.cozmicgames.game.player

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.states.InGameState
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.common.utils.Updatable
import com.cozmicgames.common.utils.extensions.unproject
import com.cozmicgames.common.utils.maths.intersectPointRect
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.gui.fill
import com.cozmicgames.game.graphics.gui.same
import com.cozmicgames.game.states.EditState
import com.cozmicgames.game.widgets.LevelCompletedWidget
import com.cozmicgames.game.widgets.SubmitLevelWidget
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

    lateinit var currentState: InGameState

    var isPaused = false

    var levelRunTime = 0.0f
        private set

    var isLevelCompleted = false
        private set

    private var levelStartTime = 0L
    private var currentLevelUuid = ""
    private var currentLevelData = ""
    private val playerPosition = Vector2()
    private val cameraFollower = CameraFollower(camera, playerPosition, 0.8f)
    private var isInputPositionVisible = true

    fun isHovered(minX: Float, minY: Float, maxX: Float, maxY: Float): Boolean {
        return intersectPointRect(inputX, inputY, minX, minY, maxX, maxY)
    }

    fun isCursorPositionVisible(): Boolean {
        return isInputPositionVisible
    }

    override fun update(delta: Float) {
        if (playState != PlayState.EDIT && !isLevelCompleted) {
            playerPosition.x = scene.playerBlock?.let { it.minX + it.width * 0.5f } ?: 0.0f
            playerPosition.y = scene.playerBlock?.let { it.minY + it.height * 0.5f } ?: 0.0f
            cameraFollower.update()
            levelRunTime = (System.currentTimeMillis() - levelStartTime) / 1000.0f
        } else if (!isPaused) {
            camera.zoom *= 1.0f + (Game.input.scrollY * 0.1f)

            if (Game.input.isButtonDown(1) || Game.input.isButtonDown(2)) {
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

        isInputPositionVisible = Game.guis.isInputPositionVisible()
    }

    fun startLevel(uuid: String, data: String) {
        currentLevelUuid = uuid
        currentLevelData = data
        scene.initialize(data)
        levelStartTime = System.currentTimeMillis()
        isLevelCompleted = false
        isPaused = false
    }

    fun resetLevel() {
        scene.initialize(currentLevelData)
    }

    fun onCompleteLevel() {
        if (isPaused)
            return

        isLevelCompleted = true

        when (playState) {
            PlayState.PLAY -> {
                val time = System.currentTimeMillis() - levelStartTime

                isPaused = true
                currentState.gui.isInteractionEnabled = false
                val window = Game.guis.openWindow("", 600.0f, 500.0f, false, false, false)
                val widget = LevelCompletedWidget(currentLevelUuid, time) {
                    if (it) {
                        //TODO: Back to menu
                    }
                    Game.tasks.submit({
                        Game.guis.closeWindow(window)
                        currentState.gui.isInteractionEnabled = true
                    })
                }.also {
                    it.constraints.x = same()
                    it.constraints.y = same()
                    it.constraints.width = fill()
                    it.constraints.height = fill()
                }
                window.content.addElement(widget)
            }

            PlayState.TEST -> {
                isPaused = true
                currentState.gui.isInteractionEnabled = false
                val window = Game.guis.openWindow("", 600.0f, 500.0f, false, false, false)
                val widget: SubmitLevelWidget = SubmitLevelWidget(currentLevelData) {
                    if (it) {
                        //TODO: Back to menu
                    } else
                        currentState.returnState = EditState(currentLevelData)

                    Game.tasks.submit({
                        Game.guis.closeWindow(window)
                    })
                }.also {
                    it.constraints.x = same()
                    it.constraints.y = same()
                    it.constraints.width = fill()
                    it.constraints.height = fill()
                }
                window.content.addElement(widget)
                window.onClose = {
                    widget.dispose()
                }
            }

            else -> { /*What are you doing here?*/
            }
        }
    }

    fun getCurrentLevelData(): String {
        return if (playState == PlayState.EDIT) {
            val properties = Properties()
            scene.write(properties)
            properties.write()
        } else
            currentLevelData
    }

    fun registerLocalLevel(uuid: String, levelData: String) {
        val directory = Gdx.files.local("levels/$uuid/")
        val levelFile = directory.child("level.json")

        levelFile.mkdirs()
        levelFile.writeString(levelData, false)
    }
}