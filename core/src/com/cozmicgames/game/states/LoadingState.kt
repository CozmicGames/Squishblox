package com.cozmicgames.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.gui.absolute
import com.cozmicgames.game.graphics.gui.aspect
import com.cozmicgames.game.graphics.gui.center
import com.cozmicgames.game.graphics.gui.elements.Image
import com.cozmicgames.game.graphics.gui.elements.Label
import com.cozmicgames.game.graphics.gui.elements.Progressbar
import com.cozmicgames.game.graphics.gui.offset
import com.cozmicgames.game.graphics.gui.packed
import com.cozmicgames.game.graphics.gui.relative
import com.cozmicgames.game.graphics.gui.same
import com.cozmicgames.game.graphics.gui.skin.ColorDrawableValue
import java.util.ArrayDeque
import kotlin.system.measureTimeMillis

class LoadingState : GameState {
    private val gui = Game.guis.create()
    private val loadingTasks = ArrayDeque<() -> Unit>()
    private var toLoadCount = 0
    private var loadedCount = 0
    private var asyncToLoadCount = 0
    private var asyncLoadedCount = 0
    private var isTexturePackingStarted = false
    private var isTexturePackingFinished = false
    private var currentInfoMessage = "Initializing"

    private val bannerImage: Image
    private val versionLabel: Label
    private val progressBar: Progressbar
    private val infoLabel: Label

    init {
        Game.textures.loadTextureSingle(Gdx.files.internal("branding/banner.png"), TextureFilter.Linear)

        if (Gdx.files.internal("fonts.txt").exists())
            Gdx.files.internal("fonts.txt").readString().lines().forEach {
                val file = Gdx.files.internal(it)
                if (it.isNotEmpty() && file.exists()) {
                    toLoadCount++
                    loadingTasks += {
                        Gdx.app.log("LOADING", "Loading $file.")
                        currentInfoMessage = "Loading $file."
                        if (!Game.fonts.loadFont(file))
                            Gdx.app.log("LOADING", "Failed to load $file.")

                        loadedCount++
                    }
                }
            }

        if (Gdx.files.internal("shaders.txt").exists())
            Gdx.files.internal("shaders.txt").readString().lines().forEach {
                val file = Gdx.files.internal(it)
                if (it.isNotEmpty() && file.exists()) {
                    toLoadCount++
                    loadingTasks += {
                        Gdx.app.log("LOADING", "Loading $file.")
                        currentInfoMessage = "Loading $file."
                        if (!Game.shaders.loadShader(file))
                            Gdx.app.log("LOADING", "Failed to load $file.")

                        loadedCount++
                    }
                }
            }

        if (Gdx.files.internal("textures.txt").exists()) {
            var filter = TextureFilter.Linear

            Gdx.files.internal("textures.txt").readString().lines().forEach { line ->
                if (line.startsWith("#")) {
                    val value = line.removePrefix("#").trim()
                    TextureFilter.values().find {
                        it.toString().equals(value, true)
                    }?.let {
                        filter = it
                    }
                } else if (line.isNotEmpty()) {
                    val file = Gdx.files.internal(line)
                    if (file.exists()) {
                        toLoadCount++
                        asyncToLoadCount++
                        loadingTasks += {
                            Gdx.app.log("LOADING", "Loading $file.")
                            currentInfoMessage = "Loading $file."

                            val isLoading = Game.textures.loadTextureAsync(file, filter) {
                                loadedCount++
                                asyncLoadedCount++
                            }

                            if (!isLoading) {
                                Gdx.app.log("LOADING", "Failed to load $file.")
                                toLoadCount--
                                asyncToLoadCount--
                            }
                        }
                    }
                }
            }
        }

        if (Gdx.files.internal("sounds.txt").exists()) {
            Gdx.files.internal("sounds.txt").readString().lines().forEach { line ->
                if (line.isNotEmpty()) {
                    val file = Gdx.files.internal(line)
                    if (file.exists()) {
                        toLoadCount++
                        asyncToLoadCount++
                        loadingTasks += {
                            Gdx.app.log("LOADING", "Loading $file.")
                            currentInfoMessage = "Loading $file."

                            val isLoading = Game.audio.loadSoundAsync(file) {
                                loadedCount++
                                asyncLoadedCount++
                            }

                            if (!isLoading) {
                                Gdx.app.log("LOADING", "Failed to load $file.")
                                toLoadCount--
                                asyncToLoadCount--
                            }
                        }
                    }
                }
            }
        }

        bannerImage = Image("branding/banner.png", flipY = true)
        bannerImage.constraints.x = center()
        bannerImage.constraints.y = absolute(25.0f)
        bannerImage.constraints.width = relative(0.6f)
        bannerImage.constraints.height = aspect(1200.0f / 500.0f)

        progressBar = Progressbar(Progressbar.ProgressbarStyle().also {
            it.background = ColorDrawableValue().also {
                it.color.set(Color.DARK_GRAY)
            }
            it.foreground = ColorDrawableValue().also {
                it.color.set(0x9A9A9AFF.toInt())
            }
        })
        progressBar.constraints.x = center()
        progressBar.constraints.y = relative(0.85f)
        progressBar.constraints.width = same(bannerImage)
        progressBar.constraints.height = absolute(15.0f)

        infoLabel = Label({ currentInfoMessage }, Label.LabelStyle().also {
            it.wrap.value = false
            it.background = null
            it.textColor.color.set(0xBABABAFF.toInt())
        })
        infoLabel.constraints.x = center()
        infoLabel.constraints.y = offset(progressBar, 5.0f)
        infoLabel.constraints.width = same(progressBar)
        infoLabel.constraints.height = absolute(16.0f)

        versionLabel = Label({ Version.versionString }, Label.LabelStyle().also {
            it.wrap.value = false
            it.background = null
            it.isFixedTextSize.value = false
            it.textColor.color.set(0xBABABAFF.toInt())
        })
        versionLabel.constraints.x = absolute(3.0f, true)
        versionLabel.constraints.y = absolute(3.0f, true)
        versionLabel.constraints.width = packed()
        versionLabel.constraints.height = absolute(12.0f)

        gui.addElement(bannerImage)
        gui.addElement(progressBar)
        gui.addElement(infoLabel)
        gui.addElement(versionLabel)
    }

    override fun render(delta: Float): () -> GameState {
        var usedTime = 0L
        while (usedTime < 10 && loadingTasks.isNotEmpty()) {
            usedTime += measureTimeMillis {
                loadingTasks.poll()()
            }
        }

        progressBar.progress = loadedCount / toLoadCount.toFloat()

        val isLoadingFinished = loadedCount == toLoadCount
        val isAsyncLoadingFinished = asyncLoadedCount == asyncToLoadCount

        if (isAsyncLoadingFinished && !isTexturePackingStarted) {
            Gdx.app.log("LOADING", "Packing textures.")
            currentInfoMessage = "Packing textures."

            Game.textures.packAtlasesAsync {
                isTexturePackingFinished = true
            }

            isTexturePackingStarted = true
        }

        if (isLoadingFinished && isTexturePackingFinished) {
            gui.removeElement(progressBar)
            gui.removeElement(infoLabel)

            return { TransitionGameState(LocalLevelsState(), LinearTransition(LinearTransition.Direction.DOWN)) }
        }

        Game.renderGraph.render(Game.time.delta)

        return { this }
    }

    override fun end() {
        Game.guis.remove(gui)
    }
}