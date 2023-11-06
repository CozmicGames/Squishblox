package com.cozmicgames.game.graphics.engine.graphics2d.particles.spawners

import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleSpawner
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.PositionData

class PointSpawner(val point: Vector2) : ParticleSpawner {
    override fun spawn(data: ParticleData, start: Int, end: Int) {
        val positions = data.getArray { PositionData() }

        repeat(end - start) {
            positions[it + start].x = point.x
            positions[it + start].y = point.y
        }
    }
}