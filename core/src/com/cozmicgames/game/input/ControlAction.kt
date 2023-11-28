package com.cozmicgames.game.input

import com.cozmicgames.game.Game
import com.cozmicgames.common.utils.Properties
import com.cozmicgames.common.utils.Reflection
import com.cozmicgames.game.input
import com.cozmicgames.game.tasks
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class ControlAction(var name: String) {
    var deadZone = 0.01f
    var rampUpSpeed = Float.MAX_VALUE
    var rampDownSpeed = Float.MAX_VALUE

    val state get() = abs(currentValueRaw) < deadZone

    var currentValue = 0.0f
        private set

    var currentValueRaw = 0.0f
        private set

    var isTriggered = false
        private set

    private val inputs = hashMapOf<KClass<*>, ControlInput>()

    fun update(delta: Float) {
        inputs.forEach { (_, input) ->
            input.update(this)
        }

        isTriggered = inputs.any { (_, input) -> input.isTriggered }
        currentValueRaw = inputs.maxOf { (_, input) -> input.currentValue }

        if (currentValueRaw <= deadZone) {
            currentValue -= rampDownSpeed * delta
            currentValue = max(0.0f, currentValue)
        } else {
            currentValue += rampUpSpeed * delta
            currentValue = min(currentValueRaw, currentValue)
        }
    }

    fun write(properties: Properties) {
        properties.setFloat("rampUpSpeed", rampUpSpeed)
        properties.setFloat("rampDownSpeed", rampDownSpeed)
        properties.setFloat("deadZone", deadZone)

        val inputsProperties = arrayListOf<Properties>()
        inputs.forEach { (type, input) ->
            val inputProperties = Properties()
            val typeName = Reflection.getClassName(type)
            inputProperties.setString("type", typeName)
            input.write(inputProperties)
            inputsProperties += inputProperties
        }

        properties.setPropertiesArray("inputs", inputsProperties.toTypedArray())
    }

    fun read(properties: Properties) {
        rampUpSpeed = properties.getFloat("rampUpSpeed") ?: Float.MAX_VALUE
        rampDownSpeed = properties.getFloat("rampDownSpeed") ?: Float.MAX_VALUE
        deadZone = properties.getFloat("deadZone") ?: 0.01f

        properties.getPropertiesArray("inputs")?.let {
            for (inputProperties in it) {
                val typeName = inputProperties.getString("type") ?: continue
                val type = Reflection.getClassByName(typeName) ?: continue
                val input = inputs.getOrPut(type) { Reflection.createInstance(type) as ControlInput }
                input.read(inputProperties)
            }
        }
    }

    fun addKey(key: Int) {
        val input = inputs.getOrPut(KeyControlInput::class) { KeyControlInput() } as KeyControlInput

        input.keys += key
    }

    fun removeKey(key: Int) {
        val input = (inputs[KeyControlInput::class] as? KeyControlInput) ?: return
        input.keys -= key

        if (input.keys.isEmpty())
            inputs.remove(KeyControlInput::class)
    }

    fun clearKeys() {
        inputs.remove(KeyControlInput::class)
    }

    fun getKeys(): Set<Int> {
        val input = (inputs[KeyControlInput::class] as? KeyControlInput) ?: return emptySet()
        return input.keys.toSet()
    }

    fun addMouseButton(button: Int) {
        val input = inputs.getOrPut(MouseButtonControlInput::class) { MouseButtonControlInput() } as MouseButtonControlInput

        input.buttons += button
    }

    fun removeMouseButton(button: Int) {
        val input = (inputs[MouseButtonControlInput::class] as? MouseButtonControlInput) ?: return
        input.buttons -= button

        if (input.buttons.isEmpty())
            inputs.remove(MouseButtonControlInput::class)
    }

    fun clearMouseButtons() {
        inputs.remove(MouseButtonControlInput::class)
    }

    fun getMouseButtons(): Set<Int> {
        val input = (inputs[MouseButtonControlInput::class] as? MouseButtonControlInput) ?: return emptySet()
        return input.buttons.toSet()
    }

    fun setDeltaX() {
        inputs.getOrPut(MouseDeltaXControlInput::class) { MouseDeltaXControlInput() }
    }

    fun unsetDeltaX() {
        inputs.remove(MouseDeltaXControlInput::class)
    }

    fun isDeltaX(): Boolean {
        return MouseDeltaXControlInput::class in inputs
    }

    fun setDeltaY() {
        inputs.getOrPut(MouseDeltaYControlInput::class) { MouseDeltaYControlInput() }
    }

    fun unsetDeltaY() {
        inputs.remove(MouseDeltaYControlInput::class)
    }

    fun isDeltaY(): Boolean {
        return MouseDeltaYControlInput::class in inputs
    }
}

fun ControlAction.addNextKey(filter: (Int) -> Boolean = { true }, cancel: () -> Boolean = { false }) {
    val listener = object : InputListener {
        override fun onKey(key: Int, down: Boolean) {
            if (cancel()) {
                Game.tasks.submit({
                    Game.input.removeListener(this)
                })

                return
            }

            if (down && filter(key)) {
                addKey(key)

                Game.tasks.submit({
                    Game.input.removeListener(this)
                })
            }
        }
    }

    Game.input.addListener(listener)
}

fun ControlAction.addNextMouseButton(filter: (Int) -> Boolean = { true }, cancel: () -> Boolean = { false }) {
    val listener = object : InputListener {
        override fun onMouseButton(button: Int, down: Boolean) {
            if (cancel()) {
                Game.tasks.submit({
                    Game.input.removeListener(this)
                })

                return
            }

            if (down && filter(button)) {
                addMouseButton(button)

                Game.tasks.submit({
                    Game.input.removeListener(this)
                })
            }
        }
    }

    Game.input.addListener(listener)
}
