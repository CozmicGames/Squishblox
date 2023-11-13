package com.cozmicgames.game.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.Game
import com.cozmicgames.game.tasks
import kotlin.reflect.KProperty

class AudioManager : Disposable {
    inner class Getter(private val file: FileHandle) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrLoadSound(file)
    }

    private val sounds = hashMapOf<String, Sound>()

    fun loadSound(file: FileHandle, name: String = file.path()): Boolean {
        if (!file.exists())
            return false

        try {
            sounds.put(name, Gdx.audio.newSound(file))?.dispose()
        } catch (_: Exception) {
            return false
        }
        return true
    }

    fun loadSoundAsync(file: FileHandle, name: String = file.path(), callback: () -> Unit): Boolean {
        if (!file.exists())
            return false

        Game.tasks.submitAsync({
            val sound = Gdx.audio.newSound(file)
            Gdx.app.postRunnable {
                sounds.put(name, sound)?.dispose()
                callback()
            }
        })
        return true
    }

    fun getSound(file: FileHandle) = getSound(file.path())

    fun getSound(name: String) = sounds[name]

    fun getOrLoadSound(file: FileHandle): Sound {
        if (getSound(file) == null)
            loadSound(file)

        return getSound(file)!!
    }

    override fun dispose() {
        sounds.forEach { (_, sound) ->
            sound.dispose()
        }
    }
}