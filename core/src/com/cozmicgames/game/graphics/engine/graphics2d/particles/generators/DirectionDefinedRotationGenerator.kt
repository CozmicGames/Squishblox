package com.cozmicgames.game.graphics.engine.graphics2d.particles.generators

import com.cozmicgames.common.utils.maths.HALF_PI
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleGenerator
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.AngleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.VelocityData
import kotlin.math.atan2

class DirectionDefinedRotationGenerator(var minStartAngle: Float, val maxStartAngle: Float, var minEndAngle: Float, val maxEndAngle: Float) : ParticleGenerator {
    override fun generate(data: ParticleData, start: Int, end: Int) {
        val angles = data.getArray { AngleData() }
        val velocities = data.getArray { VelocityData() }
        repeat(end - start) {
            with(angles[it + start]) {
                val phi = HALF_PI - atan2(-velocities[it].y, velocities[it].x)
                startAngle = phi
                endAngle = phi
                angle = phi
            }
        }
    }
}