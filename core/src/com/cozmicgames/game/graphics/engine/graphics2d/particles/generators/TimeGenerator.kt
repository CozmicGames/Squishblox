package com.cozmicgames.game.graphics.engine.graphics2d.particles.generators

import com.cozmicgames.common.utils.maths.lerp
import com.cozmicgames.common.utils.maths.randomFloat
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleGenerator
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.TimeData

class TimeGenerator(var minLifeTime: Float, var maxLifeTime: Float) : ParticleGenerator {
    override fun generate(data: ParticleData, start: Int, end: Int) {
        val times = data.getArray { TimeData() }

        repeat(end - start) {
            with(times[it + start]) {
                lifeTime = lerp(minLifeTime, maxLifeTime, randomFloat())
                remainingLifeTime = lifeTime
                interpolationValue = 0.0f
            }
        }
    }
}