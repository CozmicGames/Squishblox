package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.math.Interpolation
import com.cozmicgames.game.utils.collections.FixedSizeStack

class GUITransform(val element: GUIElement) {
    open class State(val constraints: GUIConstraints, var alpha: Float = 1.0f)

    var x = 0.0f
        internal set
        get() = if (isFirstUpdate) element.constraints.x.getValue(element.parent, element) else field

    var y = 0.0f
        internal set
        get() = if (isFirstUpdate) element.constraints.y.getValue(element.parent, element) else field

    var width = 0.0f
        internal set
        get() = if (isFirstUpdate) element.constraints.width.getValue(element.parent, element) else field

    var height = 0.0f
        internal set
        get() = if (isFirstUpdate) element.constraints.height.getValue(element.parent, element) else field

    var alpha = 1.0f
        internal set
        get() = if (transitions.isEmpty())
            1.0f
        else
            field


    /**
     * Only keep 10 states as a history, this should be more than enough for any practical reason.
     * For transitioning back to a previous state you should use 'transitionBack' anyways.
     */
    private val stateStack = FixedSizeStack<State>(10)
    private val transitions = arrayListOf<GUITransition>()
    private var canQueueTransition = true
    private var isFirstUpdate = true

    fun createState(block: State.() -> Unit): State {
        val state = State(GUIConstraints().also {
            it.x = (stateStack.current?.constraints?.x) ?: element.constraints.x
            it.y = (stateStack.current?.constraints?.y) ?: element.constraints.y
            it.width = (stateStack.current?.constraints?.width) ?: element.constraints.width
            it.height = (stateStack.current?.constraints?.height) ?: element.constraints.height
        }, stateStack.current?.alpha ?: 1.0f)
        block(state)
        return state
    }

    fun transitionTo(state: State, duration: Float, interpolation: Interpolation = Interpolation.linear, onCompletion: () -> Unit = {}) {
        if (!canQueueTransition)
            return

        val startState = transitions.lastOrNull()?.to ?: if (stateStack.isEmpty) State(element.constraints, 1.0f) else stateStack.current!!
        stateStack.push(state)

        transitions += GUITransition(this, startState, state, duration, interpolation) {
            onCompletion()
        }
    }

    fun transitionBack(duration: Float, interpolation: Interpolation = Interpolation.linear, onCompletion: () -> Unit = {}) {
        if (stateStack.isEmpty) {
            onCompletion()
            return
        }

        val fromState = stateStack.current!!
        stateStack.pop()
        val toState = if (stateStack.isEmpty) State(element.constraints, 1.0f) else stateStack.current!!

        canQueueTransition = false

        transitions += GUITransition(this, fromState, toState, duration, interpolation) {
            canQueueTransition = true
            onCompletion()
        }
    }

    fun update(delta: Float) {
        if (isFirstUpdate) {
            isFirstUpdate = false
        }

        var remainingDelta = delta
        while (transitions.isNotEmpty() && remainingDelta > 0.0f) {
            val transition = transitions.firstOrNull() ?: return
            remainingDelta = transition.update(delta)
            if (transition.isCompleted)
                transitions.removeFirst()
        }

        if (transitions.isEmpty()) {
            x = stateStack.current?.constraints?.x?.getValue(element.parent, element) ?: element.constraints.x.getValue(element.parent, element)
            y = stateStack.current?.constraints?.y?.getValue(element.parent, element) ?: element.constraints.y.getValue(element.parent, element)
            width = stateStack.current?.constraints?.width?.getValue(element.parent, element) ?: element.constraints.width.getValue(element.parent, element)
            height = stateStack.current?.constraints?.height?.getValue(element.parent, element) ?: element.constraints.height.getValue(element.parent, element)
        }
    }

    fun applyCurrentStateToElement() {
        if (stateStack.isEmpty)
            return

        element.constraints.x = stateStack.current!!.constraints.x
        element.constraints.y = stateStack.current!!.constraints.y
        element.constraints.width = stateStack.current!!.constraints.width
        element.constraints.height = stateStack.current!!.constraints.height
        stateStack.clear()
    }

    fun reset() {
        stateStack.clear()
        transitions.clear()
    }
}