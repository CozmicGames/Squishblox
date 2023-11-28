package com.cozmicgames.game.graphics.gui

import com.badlogic.gdx.math.Interpolation
import com.cozmicgames.common.utils.extensions.clamp
import com.cozmicgames.common.utils.maths.lerp
import kotlin.math.min

class GUITransition(val transform: GUITransform, val from: GUITransform.State, val to: GUITransform.State, val duration: Float, val interpolation: Interpolation = Interpolation.linear, val onCompletion: () -> Unit = {}) {
    val isCompleted get() = progress >= duration

    private var progress = 0.0f

    fun update(delta: Float): Float {
        val neededDelta = min(delta, duration - progress)
        progress += neededDelta
        val t = interpolation.apply(progress / duration).clamp(0.0f, 1.0f)
        transform.x = lerp(from.constraints.x.getValue(transform.element.parent, transform.element), to.constraints.x.getValue(transform.element.parent, transform.element), t)
        transform.y = lerp(from.constraints.y.getValue(transform.element.parent, transform.element), to.constraints.y.getValue(transform.element.parent, transform.element), t)
        transform.width = lerp(from.constraints.width.getValue(transform.element.parent, transform.element), to.constraints.width.getValue(transform.element.parent, transform.element), t)
        transform.height = lerp(from.constraints.height.getValue(transform.element.parent, transform.element), to.constraints.height.getValue(transform.element.parent, transform.element), t)
        transform.alpha = lerp(from.alpha, to.alpha, t)

        if (isCompleted)
            onCompletion()

        return delta - neededDelta
    }
}