package com.cozmicgames.game.player

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.math.Vector2
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.common.utils.Updatable
import com.cozmicgames.common.utils.extensions.unproject
import com.cozmicgames.common.utils.maths.intersectPointRect
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.gui.fill
import com.cozmicgames.game.graphics.gui.same
import com.cozmicgames.game.states.*
import com.cozmicgames.game.widgets.InfoWidget
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

    val currentGui get() = if (::currentState.isInitialized) currentState.gui else null

    var isPaused = false

    var levelRunTime = 0.0f
        private set

    var isLevelCompleted = false
        private set

    private var levelStartTime = 0L
    private var currentLevelUuid: String? = null
    private var currentLevelData = ""
    private val playerPosition = Vector2()
    private val cameraFollower = CameraFollower(camera, playerPosition, 0.8f)
    private var previewImageIndex = 0

    fun isHovered(minX: Float, minY: Float, maxX: Float, maxY: Float): Boolean {
        return intersectPointRect(inputX, inputY, minX, minY, maxX, maxY)
    }

    fun isCursorPositionVisible(): Boolean {
        return currentGui?.isInputPositionVisible == true
    }

    override fun update(delta: Float) {
        previewImageIndex = 0

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
    }

    fun startLevel(uuid: String?, data: String) {
        currentLevelUuid = uuid
        currentLevelData = data
        scene.initialize(currentLevelData)
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
            PlayState.TUTORIAL -> {
                Game.audio.playSound("sounds/level_completed.wav")
                Game.gameSettings.playedTutorial = true

                isPaused = true
                val window = currentGui!!.openWindow("", 500.0f, 450.0f, false, false, false, true)
                val widget = InfoWidget("Tutorial completed", "You completed the tutorial.\nNow create, explore and play\nyour own or others levels!") {
                    currentState.returnState = TransitionGameState(LocalLevelsState(), CircleTransition())

                    Game.tasks.submit({
                        window.close()
                        currentGui!!.isInteractionEnabled = true
                    })
                }.also {
                    it.constraints.x = same()
                    it.constraints.y = same()
                    it.constraints.width = fill()
                    it.constraints.height = fill()
                }
                window.content.addElement(widget)
            }

            PlayState.PLAY -> {
                Game.audio.playSound("sounds/level_completed.wav")

                val time = System.currentTimeMillis() - levelStartTime

                isPaused = true
                val window = currentGui!!.openWindow("", 600.0f, 500.0f, false, false, false, true)
                val widget = LevelCompletedWidget(currentLevelUuid!!, time) {
                    currentState.returnState = TransitionGameState(LocalLevelsState(), CircleTransition())

                    Game.tasks.submit({
                        window.close()
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
                Game.audio.playSound("sounds/level_completed.wav")

                isPaused = true
                val window = currentGui!!.openWindow("", 1000.0f, 800.0f, false, false, false, true)
                val widget = SubmitLevelWidget(currentLevelData, currentLevelUuid) {
                    Game.tasks.submit({
                        if (it)
                            currentState.returnState = TransitionGameState(LocalLevelsState(), CircleTransition())
                        else
                            currentState.returnState = EditState(currentLevelData)

                        window.close()
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

    fun registerLocalLevel(uuid: String, levelProperties: Properties) {
        val levelFile = Gdx.files.local("levels/local/$uuid/level.json")
        if (levelFile.exists())
            levelFile.delete()
        levelFile.writeString(levelProperties.write(false), false)

        val imageFile = Gdx.files.local("levels/local/$uuid/level.png")
        if (imageFile.exists())
            imageFile.delete()
        val image = createPreviewImage(levelProperties)
        PixmapIO.writePNG(imageFile, image)
        image.dispose()
    }

    fun registerSavedLevel(uuid: String, levelProperties: Properties) {
        val levelFile = Gdx.files.local("levels/saved/$uuid/level.json")
        levelFile.writeString(levelProperties.write(false), false)

        val image = createPreviewImage(levelProperties)
        PixmapIO.writePNG(Gdx.files.local("levels/saved/$uuid/level.png"), image)
        image.dispose()
    }

    fun createPreviewImage(levelProperties: Properties): Pixmap {
        val camera = PlayerCamera()

        levelProperties.getProperties("camera")?.let {
            camera.position.x = it.getFloat("x") ?: 0.0f
            camera.position.y = it.getFloat("y") ?: 0.0f
            camera.zoom = it.getFloat("zoom") ?: 1.0f
            camera.update()
        }

        var sceneData = ""
        levelProperties.getProperties("level")?.let {
            sceneData = it.write()
        }

        return Game.previewImageRenderer.renderToImage(camera, sceneData, previewImageIndex++)
    }

    fun deleteLocalLevel(uuid: String) {
        val directory = Gdx.files.local("levels/local/$uuid")

        if (directory.exists())
            directory.deleteDirectory()
    }

    fun deleteSavedLevel(uuid: String) {
        val directory = Gdx.files.local("levels/saved/$uuid")

        if (directory.exists())
            directory.deleteDirectory()
    }
}