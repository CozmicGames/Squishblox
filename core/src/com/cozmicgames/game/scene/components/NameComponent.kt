package com.cozmicgames.game.scene.components

import com.badlogic.gdx.Gdx
import com.cozmicgames.game.scene.Component
import com.cozmicgames.game.scene.GameObject
import com.cozmicgames.game.scene.Scene
import com.cozmicgames.common.utils.Properties

class NameComponent : Component() {
    var name = ""
        set(value) {
            if (gameObject.scene.findGameObjectByName(value) != null) {
                Gdx.app.error("ERROR", "The name $value already exists. Names must be unique across the same scene!")
                return
            }

            field = value
        }

    override fun read(properties: Properties) {
        properties.getString("name")?.let { name = it }
    }

    override fun write(properties: Properties) {
        properties.setString("name", name)
    }
}

fun Scene.findGameObjectByName(name: String): GameObject? {
    gameObjects.forEach {
        val nameComponent = it.getComponent<NameComponent>()
        if (nameComponent?.name == name)
            return it
    }

    return null
}
