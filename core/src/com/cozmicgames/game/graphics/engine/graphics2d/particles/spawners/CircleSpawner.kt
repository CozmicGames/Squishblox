package com.cozmicgames.game.graphics.engine.graphics2d.particles.spawners

import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.utils.maths.TWO_PI
import com.cozmicgames.game.utils.maths.randomFloat
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleSpawner
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.PositionData
import kotlin.math.cos
import kotlin.math.sin

class CircleSpawner(val center: Vector2, var radius: Float) : ParticleSpawner {
    override fun spawn(data: ParticleData, start: Int, end: Int) {
        val positions = data.getArray { PositionData() }

        repeat(end - start) {
            val phi = randomFloat() * TWO_PI

            positions[it + start].x = center.x + radius * cos(phi)
            positions[it + start].y = center.y + radius * sin(phi)
        }
    }
}