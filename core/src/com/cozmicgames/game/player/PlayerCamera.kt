package com.cozmicgames.game.player

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.cozmicgames.common.utils.extensions.clamp
import com.cozmicgames.common.utils.extensions.safeHeight
import com.cozmicgames.common.utils.extensions.safeWidth

class PlayerCamera {
    companion object {
        private val VIEWPORT_POINTS_NDC = arrayOf(
            Vector3(-1.0f, -1.0f, 0.0f),
            Vector3(1.0f, -1.0f, 0.0f),
            Vector3(1.0f, 1.0f, 0.0f),
            Vector3(-1.0f, 1.0f, 0.0f)
        )
    }

    class State(val x: Float, val y: Float, val zoom: Float)

    val camera = OrthographicCamera()

    val position by camera::position
    var zoom by camera::zoom

    val rectangle = Rectangle()

    var getViewportWidth: (() -> Float)? = null
    var getViewportHeight: (() -> Float)? = null
    var getMinZoom: (() -> Float)? = null
    var getMaxZoom: (() -> Float)? = null
    var getMinX: (() -> Float)? = null
    var getMinY: (() -> Float)? = null
    var getMaxX: (() -> Float)? = null
    var getMaxY: (() -> Float)? = null
    var isLocked = false

    private val viewportPoints = Array(VIEWPORT_POINTS_NDC.size) { Vector3() }

    init {
        camera.setToOrtho(false)
    }

    fun update() {
        camera.viewportWidth = getViewportWidth?.invoke() ?: Gdx.graphics.safeWidth.toFloat()
        camera.viewportHeight = getViewportHeight?.invoke() ?: Gdx.graphics.safeHeight.toFloat()

        val minZoom = getMinZoom?.invoke() ?: 0.0f
        val maxZoom = getMaxZoom?.invoke() ?: Float.MAX_VALUE
        zoom = zoom.clamp(minZoom, maxZoom)

        getMinX?.invoke()?.let {
            if (position.x < it)
                position.x = it
        }

        getMinY?.invoke()?.let {
            if (position.y < it)
                position.y = it
        }

        getMaxX?.invoke()?.let {
            if (position.x > it)
                position.x = it
        }

        getMaxY?.invoke()?.let {
            if (position.y > it)
                position.y = it
        }

        camera.update()

        repeat(viewportPoints.size) {
            viewportPoints[it].set(VIEWPORT_POINTS_NDC[it])
            viewportPoints[it].mul(camera.invProjectionView)
        }

        val cullingMinX = viewportPoints.minBy { it.x }.x
        val cullingMinY = viewportPoints.minBy { it.y }.y
        val cullingMaxX = viewportPoints.maxBy { it.x }.x
        val cullingMaxY = viewportPoints.maxBy { it.y }.y

        rectangle.x = cullingMinX
        rectangle.y = cullingMinY
        rectangle.width = cullingMaxX - cullingMinX
        rectangle.height = cullingMaxY - cullingMinY
    }

    fun createState() = State(position.x, position.y, zoom)

    fun setState(state: State) {
        position.x = state.x
        position.y = state.y
        zoom = state.zoom
    }
}