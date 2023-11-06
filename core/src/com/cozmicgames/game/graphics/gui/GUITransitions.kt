package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.math.Interpolation

private val DEFAULT_INTERPOLATION = Interpolation.linear
private val DEFAULT_DURATION = 0.10f

fun GUIElement.slideOutLeft(duration: Float = DEFAULT_DURATION, interpolation: Interpolation = DEFAULT_INTERPOLATION, onCompletion: () -> Unit = {}) {
    transform.transitionTo(transform.createState {
        constraints.x = absolute { -width }
    }, duration, interpolation, onCompletion)
}

fun GUIElement.slideOutRight(duration: Float = DEFAULT_DURATION, interpolation: Interpolation = DEFAULT_INTERPOLATION, onCompletion: () -> Unit = {}) {
    transform.transitionTo(transform.createState {
        constraints.x = absolute(0.0f, true) + absolute { width }
    }, duration, interpolation, onCompletion)
}

fun GUIElement.slideOutBottom(duration: Float = DEFAULT_DURATION, interpolation: Interpolation = DEFAULT_INTERPOLATION, onCompletion: () -> Unit = {}) {
    transform.transitionTo(transform.createState {
        constraints.y = absolute(0.0f, true) + absolute { height }
    }, duration, interpolation, onCompletion)
}

fun GUIElement.slideOutTop(duration: Float = DEFAULT_DURATION, interpolation: Interpolation = DEFAULT_INTERPOLATION, onCompletion: () -> Unit = {}) {
    transform.transitionTo(transform.createState {
        constraints.y = absolute { -height }
    }, duration, interpolation, onCompletion)
}

fun GUIElement.transitionTo(block: GUITransform.State.() -> Unit, duration: Float = DEFAULT_DURATION, interpolation: Interpolation = DEFAULT_INTERPOLATION, onCompletion: () -> Unit = {}) {
    transform.transitionTo(transform.createState(block), duration, interpolation, onCompletion)
}

fun GUIElement.transitionBack(duration: Float = DEFAULT_DURATION, interpolation: Interpolation = DEFAULT_INTERPOLATION, onCompletion: () -> Unit = {}) {
    transform.transitionBack(duration, interpolation, onCompletion)
}
