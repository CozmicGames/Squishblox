package com.cozmicgames.game.graphics.engine.graphics2d.particles.generators

import com.badlogic.gdx.math.Vector2
import com.cozmicgames.common.utils.maths.lerp
import com.cozmicgames.common.utils.maths.randomFloat
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleGenerator
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.VelocityData

class VectorVelocityGenerator(var minVelocity: Vector2, val maxVelocity: Vector2) : ParticleGenerator {
    override fun generate(data: ParticleData, start: Int, end: Int) {
        val velocities = data.getArray { VelocityData() }

        repeat(end - start) {
            val r = randomFloat()
            velocities[it + start].x = lerp(minVelocity.x, maxVelocity.x, r)
            velocities[it + start].y = lerp(minVelocity.y, maxVelocity.y, r)
        }
    }
}