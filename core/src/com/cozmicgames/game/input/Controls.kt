package com.cozmicgames.game.input

import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.utils.Updatable

class Controls : Updatable {
    private val actionsInternal = arrayListOf<ControlAction>()

    val actions get() = actionsInternal.toList()

    fun add(name: String): ControlAction {
        val action = ControlAction(name)
        return action
    }

    fun find(name: String): ControlAction? {
        return actionsInternal.find { it.name == name }
    }

    fun remove(name: String): Boolean {
        return actionsInternal.removeIf { it.name == name }
    }

    fun clear() {
        actionsInternal.clear()
    }

    override fun update(delta: Float) {
        actionsInternal.forEach {
            it.update(delta)
        }
    }

    fun write(): String {
        val actionsProperties = arrayListOf<Properties>()

        actions.forEach {
            val actionProperties = Properties()
            actionProperties.setString("name", it.name)
            it.write(actionProperties)
            actionsProperties += actionProperties
        }

        val properties = Properties()
        properties.setPropertiesArray("actions", actionsProperties.toTypedArray())
        return properties.write()
    }

    fun read(text: String) {
        val properties = Properties()
        properties.read(text)

        properties.getPropertiesArray("actions")?.let {
            for (actionProperties in it) {
                val name = actionProperties.getString("name") ?: continue
                val action = find(name) ?: add(name)
                action.read(actionProperties)
            }
        }
    }
}