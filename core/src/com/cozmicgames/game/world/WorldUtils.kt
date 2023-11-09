package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sign

object WorldUtils {
    fun getRandomBlockColor(): Color {
        return Color.CYAN
    }

    fun toCellCoord(value: Float): Int {
        val sign = sign(value)
        val absoluteCellCoord = round(abs(value).toInt() / WorldConstants.WORLD_CELL_SIZE)
        return (sign * absoluteCellCoord).toInt()
    }

    fun toWorldCoord(value: Int): Float {
        return value * WorldConstants.WORLD_CELL_SIZE
    }

    fun roundWorldToCellCoord(value: Float) = toWorldCoord(toCellCoord(value))
}