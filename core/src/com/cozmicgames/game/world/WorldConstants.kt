package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

object WorldConstants {
    const val WORLD_CELL_SIZE = 64.0f
    const val RESIZE_BORDER_SIZE = 8.0f
    const val CLOUD_SIZE = 256.0f
    val SHADOW_COLOR = Color(0.2f, 0.2f, 0.2f, 0.5f)
    val SHADOW_OFFSET = Vector2(6.0f, 8.0f)
    const val CLOUD_SPEED = 10.0f
    const val CLOUD_Y = 1000.0f
    const val CLOUD_Y_SPREAD = 600.0f
    const val CLOUD_SPEED_SPREAD = 0.5f

    const val WORLD_MIN_X = -100 * WORLD_CELL_SIZE
    const val WORLD_MAX_X = 100 * WORLD_CELL_SIZE
    const val CLOUDS_COUNT = 100
}