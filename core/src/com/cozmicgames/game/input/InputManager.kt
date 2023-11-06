package com.cozmicgames.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.utils.IntSet
import com.cozmicgames.game.utils.Updatable

class InputManager : Updatable {
    companion object {
        private const val MAX_BUTTONCODES = 5
    }

    var isTouched = false
        private set

    var justTouchedDown = false
        private set

    var justTouchedUp = false
        private set

    var x = -Float.MAX_VALUE
        private set

    var y = -Float.MAX_VALUE
        private set

    var previousX = 0.0f
        private set

    var previousY = 0.0f
        private set

    var deltaX = 0.0f
        private set

    var deltaY = 0.0f
        private set

    var scrollX = 0.0f
        private set

    var scrollY = 0.0f
        private set

    private val keys = BooleanArray(Keys.MAX_KEYCODE)
    private val keysJustDown = BooleanArray(Keys.MAX_KEYCODE)
    private val keysJustUp = BooleanArray(Keys.MAX_KEYCODE)
    private val keyStates = BooleanArray(Keys.MAX_KEYCODE)

    private val buttons = BooleanArray(MAX_BUTTONCODES)
    private val buttonsJustDown = BooleanArray(MAX_BUTTONCODES)
    private val buttonsJustUp = BooleanArray(MAX_BUTTONCODES)
    private val buttonStates = BooleanArray(MAX_BUTTONCODES)

    private val touchedPointers = IntSet()
    private var firstUpdate = true
    private var scrollFrameIndex = 0L
    private val listeners = arrayListOf<InputListener>()

    init {
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun keyDown(keycode: Int): Boolean {
                keys[keycode] = true
                listeners.forEach {
                    it.onKey(keycode, true)
                }
                return true
            }

            override fun keyUp(keycode: Int): Boolean {
                keys[keycode] = false
                listeners.forEach {
                    it.onKey(keycode, false)
                }
                return true
            }

            override fun keyTyped(character: Char): Boolean {
                listeners.forEach {
                    it.onChar(character)
                }
                return true
            }

            override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
                x = screenX.toFloat()
                y = screenY.toFloat()
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                x = screenX.toFloat()
                y = screenY.toFloat()
                return true
            }

            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                scrollX = amountX
                scrollY = amountY
                listeners.forEach {
                    it.onScroll(amountX, amountY)
                }
                scrollFrameIndex = Gdx.graphics.frameId
                return true
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                x = screenX.toFloat()
                y = screenY.toFloat()
                buttons[button] = true
                touchedPointers.add(pointer)
                listeners.forEach {
                    it.onTouch(screenX, screenY, pointer, true)
                }
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                buttons[button] = false
                touchedPointers.remove(pointer)
                listeners.forEach {
                    it.onTouch(screenX, screenY, pointer, false)
                }
                return true
            }
        }
    }

    fun addListener(listener: InputListener) {
        listeners += listener
    }

    fun removeListener(listener: InputListener) {
        listeners -= listener
    }

    override fun update(delta: Float) {
        repeat(keys.size) {
            keysJustDown[it] = keys[it] && !keyStates[it]
            keysJustUp[it] = !keys[it] && keyStates[it]
            keyStates[it] = keys[it]
        }

        repeat(buttons.size) {
            buttonsJustDown[it] = buttons[it] && !buttonStates[it]
            buttonsJustUp[it] = !buttons[it] && buttonStates[it]
            buttonStates[it] = buttons[it]
        }

        val previousTouchState = isTouched
        isTouched = buttonStates.any { it }
        if (justTouchedDown)
            justTouchedDown = false
        else if (!previousTouchState && isTouched)
            justTouchedDown = true

        justTouchedUp = buttonsJustUp.any { it }

        if (!firstUpdate) {
            deltaX = x - previousX
            deltaY = y - previousY
        }

        previousX = x
        previousY = y

        if (Gdx.graphics.frameId > scrollFrameIndex + 1) {
            scrollX = 0.0f
            scrollY = 0.0f
        }

        firstUpdate = false
    }

    fun isKeyDown(key: Int) = keys[key]

    fun isKeyJustDown(key: Int) = keysJustDown[key]

    fun isKeyJustUp(key: Int) = keysJustUp[key]

    fun isButtonDown(button: Int) = buttons[button]

    fun isButtonJustDown(button: Int) = buttonsJustDown[button]

    fun isButtonJustUp(button: Int) = buttonsJustUp[button]

    fun isTouched(pointer: Int) = pointer in touchedPointers
}