package com.cozmicgames.game.graphics.engine.graphics2d.particles.generators

import com.cozmicgames.common.utils.maths.lerp
import com.cozmicgames.common.utils.maths.randomFloat
import com.cozmicgames.common.utils.maths.toRadians
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleGenerator
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.VelocityData
import kotlin.math.cos
import kotlin.math.sin

open class AngledVelocityGenerator(var minAngle: Float, var maxAngle: Float, var minStartSpeed: Float, var maxStartSpeed: Float) : ParticleGenerator {
    override fun generate(data: ParticleData, start: Int, end: Int) {
        val velocities = data.getArray { VelocityData() }

        repeat(end - start) {
            val phi = toRadians(lerp(minAngle, maxAngle, randomFloat()) - 90.0f)
            val speed = lerp(minStartSpeed, maxStartSpeed, randomFloat())

            velocities[it + start].x = cos(phi) * speed
            velocities[it + start].y = sin(phi) * speed
        }
    }
}