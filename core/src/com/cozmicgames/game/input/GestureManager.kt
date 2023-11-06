package com.cozmicgames.game.input

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.game.Game
import com.cozmicgames.game.utils.Updatable
import com.cozmicgames.game.input
import engine.input.GestureListener

class GestureManager : Updatable, InputListener, Disposable {
    var tapRectangleSize = 20.0f
        set(value) {
            field = value
            tapRectangle.width = value
            tapRectangle.height = value
        }

    var tapInterval = 0.4f

    var longPressDuration = 1.1f

    private val listeners = arrayListOf<GestureListener>()
    private val tapRectangle = Rectangle(0.0f, 0.0f, tapRectangleSize, tapRectangleSize)
    private var isInTapRectangle = false
    private var tapCount = 0
    private var lastTapCount = 0
    private var lastTapTime = 0L
    private var isTouched = false
    private var longPressCounter = 0.0
    private var hasLongPressFired = false
    private val pointer = Vector2()
    private val center = Vector2()
    private val lastTapPointer = Vector2()


    init {
        Game.input.addListener(this)
    }

    fun addListener(listener: GestureListener) {
        listeners += listener
    }

    fun removeListener(listener: GestureListener) {
        listeners -= listener
    }

    override fun onTouch(x: Int, y: Int, pointer: Int, down: Boolean) {
        isTouched = down

        this.pointer.set(x.toFloat(), y.toFloat())

        if (down) {
            isInTapRectangle = true
            tapRectangle.setCenter(x.toFloat(), y.toFloat())
        } else {
            if (isInTapRectangle && this.pointer !in tapRectangle)
                isInTapRectangle = false

            if (!hasLongPressFired) {
                if (isInTapRectangle) {
                    if (this.lastTapPointer !in tapRectangle || (System.currentTimeMillis() - lastTapTime) / 1000.0f > tapInterval)
                        tapCount = 0

                    tapCount++
                    lastTapTime = System.currentTimeMillis()
                    lastTapPointer.set(x.toFloat(), y.toFloat())

                    listeners.forEach {
                        tapRectangle.getCenter(center)
                        it.onTap(center.x, center.y, tapCount)
                    }
                }
            }
        }
    }

    override fun update(delta: Float) {
        if (isTouched)
            longPressCounter += delta
        else {
            longPressCounter = 0.0
            hasLongPressFired = false
        }

        if (longPressCounter >= longPressDuration && !hasLongPressFired) {
            hasLongPressFired = true

            listeners.forEach {
                it.onLongPress(pointer.x, pointer.y)
            }
        }
    }

    override fun dispose() {
        Game.input.removeListener(this)
    }
}
