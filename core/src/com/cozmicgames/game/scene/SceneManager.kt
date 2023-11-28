package com.cozmicgames.game.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.Version
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.common.utils.Updatable
import kotlin.reflect.KClass

class SceneManager : Updatable, Disposable {
    private val globalProcessors = hashMapOf<KClass<*>, () -> SceneProcessor>()
    private val scenes = hashMapOf<String, Scene>()
    private val activeScenes = hashSetOf<String>()


    fun <T : SceneProcessor> addGlobalProcessor(type: KClass<T>, supplier: () -> T) {
        globalProcessors[type] = supplier

        for ((_, scene) in scenes)
            scene.addSceneProcessor(supplier())
    }

    fun <T : SceneProcessor> removeGlobalProcessor(type: KClass<T>) {
        globalProcessors.remove(type)

        for ((_, scene) in scenes) {
            val processor = scene.findSceneProcessor(type)
            if (processor != null)
                scene.removeSceneProcessor(processor)
        }
    }

    private fun createScene(): Scene {
        val scene = Scene()
        globalProcessors.forEach { (_, supplier) ->
            scene.addSceneProcessor(supplier())
        }
        return scene
    }

    operator fun get(name: String): Scene = scenes.getOrPut(name) { createScene() }

    fun setActive(name: String, active: Boolean) {
        if (active)
            activeScenes += name
        else
            activeScenes -= name
    }

    fun isActive(name: String) = name in activeScenes

    override fun update(delta: Float) {
        scenes.forEach { (name, scene) ->
            if (isActive(name))
                scene.update(delta)
        }
    }

    fun saveScene(name: String, file: FileHandle, filter: (GameObject) -> Boolean = { true }) {
        val scene = scenes[name] ?: return

        val properties = Properties()
        try {
            scene.write(properties, filter)
        } catch (e: Exception) {
            Gdx.app.error("ERROR", "Failed to write scene data.")
            return
        }

        file.writeString(properties.write(Version.type != Version.Type.RELEASE), false)
    }

    fun loadScene(name: String, file: FileHandle) {
        val scene = scenes.getOrPut(name) { Scene() }

        val properties = Properties()
        properties.read(file.readString())

        try {
            scene.read(properties)
        } catch (e: Exception) {
            Gdx.app.error("ERROR", "Failed to read scene data.")
        }
    }

    fun saveAll(vararg names: String = scenes.keys.toTypedArray(), resolve: (String) -> FileHandle) {
        names.forEach {
            saveScene(it, resolve(it))
        }
    }

    fun loadAll(vararg names: String = scenes.keys.toTypedArray(), resolve: (String) -> FileHandle) {
        names.forEach {
            loadScene(it, resolve(it))
        }
    }

    fun clearScenes() {
        scenes.clear()
        activeScenes.clear()
    }

    fun clearGlobalProcessors() {
        globalProcessors.clear()
    }

    override fun dispose() {
        for ((_, scene) in scenes)
            scene.dispose()

        clearScenes()
        clearGlobalProcessors()
    }
}