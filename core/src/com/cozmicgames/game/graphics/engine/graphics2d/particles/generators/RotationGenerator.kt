package com.cozmicgames.game.graphics.engine.graphics2d.particles.generators

import com.cozmicgames.game.utils.maths.randomFloat
import com.cozmicgames.game.utils.maths.toRadians
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleGenerator
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.AngleData

class RotationGenerator(var minStartAngle: Float, val maxStartAngle: Float, var minEndAngle: Float, val maxEndAngle: Float) : ParticleGenerator {
    override fun generate(data: ParticleData, start: Int, end: Int) {
        val angles = data.getArray { AngleData() }

        repeat(end - start) {
            with(angles[it + start]) {
                startAngle = toRadians(minStartAngle + randomFloat() * (maxStartAngle - minStartAngle))
                endAngle = toRadians(minEndAngle + randomFloat() * (maxEndAngle - minEndAngle))
                angle = startAngle
            }
        }
    }
}