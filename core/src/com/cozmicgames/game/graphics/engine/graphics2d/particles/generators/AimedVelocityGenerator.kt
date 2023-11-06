package com.cozmicgames.game.graphics.engine.graphics2d.particles.generators

import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.utils.maths.lerp
import com.cozmicgames.game.utils.maths.randomFloat
import com.cozmicgames.game.utils.maths.normalized
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleGenerator
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.PositionData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.VelocityData

class AimedVelocityGenerator(var target: Vector2, var minStartSpeed: Float, var maxStartSpeed: Float) : ParticleGenerator {
    override fun generate(data: ParticleData, start: Int, end: Int) {
        val velocities = data.getArray { VelocityData() }
        val positions = data.getArray { PositionData() }
        repeat(end - start) {
            val dx = target.x - positions[it + start].x
            val dy = target.y - positions[it + start].y
            normalized(dx, dy) { ndx, ndy ->
                val speed = lerp(minStartSpeed, maxStartSpeed, randomFloat())

                velocities[it + start].x = ndx * speed
                velocities[it + start].y = ndy * speed
            }
        }
    }
}