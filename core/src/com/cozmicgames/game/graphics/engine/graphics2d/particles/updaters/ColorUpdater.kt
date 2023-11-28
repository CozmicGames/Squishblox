package com.cozmicgames.game.graphics.engine.graphics2d.particles.updaters

import com.cozmicgames.common.utils.maths.lerp
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleUpdater
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.ColorData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.TimeData

class ColorUpdater : ParticleUpdater {
    private lateinit var colors: Array<ColorData>
    private lateinit var times: Array<TimeData>

    override fun init(data: ParticleData) {
        colors = data.getArray { ColorData() }
        times = data.getArray { TimeData() }

    }

    override fun update(data: ParticleData, delta: Float) {
        repeat(data.numberOfAlive) {
            with(colors[it]) {
                color.r = lerp(startColor.r, endColor.r, times[it].interpolationValue)
                color.g = lerp(startColor.g, endColor.g, times[it].interpolationValue)
                color.b = lerp(startColor.b, endColor.b, times[it].interpolationValue)
                color.a = lerp(startColor.a, endColor.a, times[it].interpolationValue)
            }
        }
    }
}