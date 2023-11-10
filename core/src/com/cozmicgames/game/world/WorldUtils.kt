package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import kotlin.math.*

object WorldUtils {
    enum class CoordRounding {
        FLOOR,
        ROUND,
        CEIL
    }

    fun getRandomBlockColor(): Color {
        return Color.CYAN
    }

    fun toCellCoord(value: Float, rounding: CoordRounding = CoordRounding.ROUND): Int {
        val sign = sign(value)
        val unroundedAbsoluteCellCoord = abs(value).toInt() / WorldConstants.WORLD_CELL_SIZE
        val absoluteCellCoord = when (rounding) {
            CoordRounding.FLOOR -> floor(unroundedAbsoluteCellCoord)
            CoordRounding.ROUND -> round(unroundedAbsoluteCellCoord)
            CoordRounding.CEIL -> ceil(unroundedAbsoluteCellCoord)
        }
        return (sign * absoluteCellCoord).toInt()
    }

    fun toWorldCoord(value: Int): Float {
        return value * WorldConstants.WORLD_CELL_SIZE
    }

    fun roundWorldToCellCoord(value: Float) = toWorldCoord(toCellCoord(value))
}