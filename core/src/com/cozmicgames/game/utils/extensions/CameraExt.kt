package com.cozmicgames.game.utils.extensions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3

private val temp = Vector3()

fun Camera.unproject(x: Float, y: Float, z: Float = 0.0f, viewportX: Int = 0, viewportY: Int = 0, viewportWidth: Int = Gdx.graphics.safeWidth, viewportHeight: Int = Gdx.graphics.safeHeight, block: (Float, Float, Float) -> Unit) {
    unproject(temp.set(x, y, z), viewportX.toFloat(), viewportY.toFloat(), viewportWidth.toFloat(), viewportHeight.toFloat())
    block(temp.x, temp.y, temp.z)
}