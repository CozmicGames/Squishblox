package com.cozmicgames.game.input

import com.badlogic.gdx.Input
import com.cozmicgames.game.Game
import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.input

interface ControlInput {
    val isTriggered: Boolean
    val currentValue: Float

    fun update(action: ControlAction)

    fun read(properties: Properties) {}
    fun write(properties: Properties) {}
}

class KeyControlInput : ControlInput {
    val keys = hashSetOf<Int>()

    override var isTriggered = false
        private set

    override var currentValue = 0.0f
        private set

    override fun update(action: ControlAction) {
        isTriggered = keys.any { Game.input.isKeyJustDown(it) }
        currentValue = if (keys.any { Game.input.isKeyDown(it) }) 1.0f else 0.0f
    }

    override fun write(properties: Properties) {
        properties.setStringArray("keys", keys.map { it.toString() }.toTypedArray())
    }

    override fun read(properties: Properties) {
        properties.getStringArray("keys")?.let {
            keys.clear()

            for (keyName in it) {
                val key = Input.Keys.valueOf(keyName)
                if (key < 0)
                    continue
                keys += key
            }
        }
    }
}

class MouseButtonControlInput : ControlInput {
    val buttons = hashSetOf<Int>()

    override var isTriggered = false
        private set

    override var currentValue = 0.0f
        private set

    override fun update(action: ControlAction) {
        isTriggered = buttons.any { Game.input.isButtonJustDown(it) }
        currentValue = if (buttons.any { Game.input.isButtonDown(it) }) 1.0f else 0.0f
    }

    override fun write(properties: Properties) {
        properties.setIntArray("buttons", buttons.toTypedArray())
    }

    override fun read(properties: Properties) {
        properties.getIntArray("buttons")?.let {
            buttons.clear()
            buttons += it
        }
    }
}

class MouseDeltaXControlInput : ControlInput {
    override var isTriggered = false
        private set

    override var currentValue = 0.0f
        private set

    override fun update(action: ControlAction) {
        currentValue = Game.input.deltaX
    }
}

class MouseDeltaYControlInput : ControlInput {
    override var isTriggered = false
        private set

    override var currentValue = 0.0f
        private set

    override fun update(action: ControlAction) {
        currentValue = Game.input.deltaY
    }
}
