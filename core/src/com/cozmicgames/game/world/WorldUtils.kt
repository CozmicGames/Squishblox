package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import kotlin.math.*

object WorldUtils {
    enum class CoordRounding {
        FLOOR,
        ROUND,
        CEIL
    }

    private val blockColors = arrayOf(
        Color(0x640053FF),
        Color(0x990C4EFF.toInt()),
        Color(0xCF1749FF.toInt()),
        Color(0xFE364EFF.toInt()),
        Color(0xFC836FFF.toInt()),
        Color(0xFBB886FF.toInt()),
        Color(0xFD8D2EFF.toInt()),
        Color(0xEB670AFF.toInt()),
        Color(0xCE4707FF.toInt()),
        Color(0xAF2604FF.toInt()),
        Color(0x8D0000FF.toInt()),
        Color(0xB84B1DFF.toInt()),
        Color(0xE3963AFF.toInt()),
        Color(0xf9c565FF.toInt()),
        Color(0xfcd99cFF.toInt()),
        Color(0xffecd3FF.toInt()),
        Color(0xc6f63dFF.toInt()),
        Color(0x93d91aFF.toInt()),
        Color(0x4a9b1fFF),
        Color(0x005d24FF),
        Color(0x005a66FF),
        Color(0x198664FF),
        Color(0x32b262FF),
        Color(0x5bd170FF),
        Color(0x93e38eFF.toInt()),
        Color(0xcbf5acFF.toInt()),
        Color(0x80e8d2FF.toInt()),
        Color(0x61d5d9FF),
        Color(0x41aecbFF),
        Color(0x2075a8FF),
        Color(0x003c85FF),
        Color(0x003186FF),
        Color(0xb0b9f2FF.toInt()),
        Color(0x9a3bd5FF.toInt()),
        Color(0x6d04a9FF),
        Color(0x4f00a4FF),
        Color(0x9518c8FF.toInt()),
        Color(0xdc31edFF.toInt()),
        Color(0xfd7caeFF.toInt()),
        Color(0xfb298dFF.toInt()),
        Color(0xdc0078FF.toInt()),
        Color(0xa0006eFF.toInt()),
        Color(0x640064FF),
        Color(0x7a3b21FF),
        Color(0xffeeb0FF.toInt()),
        Color(0xa9a254FF.toInt()),
        Color(0x78803bFF),
        Color(0x3e6133FF),
        Color(0x03402bFF)
    )

    fun getRandomBlockColor(): Color {
        return blockColors.random()
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