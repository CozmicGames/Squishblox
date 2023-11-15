package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import kotlin.math.*

object WorldUtils {
    enum class CoordRounding {
        FLOOR,
        ROUND,
        CEIL
    }

    private const val COLOR_COUNT = 16

    private val blockColors = Array(COLOR_COUNT) {
        val hue = it.toFloat() / COLOR_COUNT.toFloat() * 360.0f
        getColor(hue)
    }

    fun getRandomBlockColor(): Color {
        return blockColors.random()
    }

    fun getColor(hue: Float): Color {
        val s = 0.75f
        val v = 1.0f
        return Color(1.0f, 1.0f, 1.0f, 1.0f).fromHsv(hue, s, v)
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

    fun approach(start: Float, target: Float, increment: Float): Float {
        var value = start
        val absoluteIncrement = abs(increment)
        if (value < target) {
            value += absoluteIncrement
            if (value > target)
                value = target
        } else {
            value -= absoluteIncrement
            if (value < target)
                value = target
        }
        return value
    }
}