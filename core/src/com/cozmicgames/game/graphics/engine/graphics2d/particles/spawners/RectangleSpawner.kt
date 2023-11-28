package com.cozmicgames.game.graphics.engine.graphics2d.particles.spawners

import com.badlogic.gdx.math.Rectangle
import com.cozmicgames.common.utils.maths.randomFloat
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleSpawner
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.PositionData

class RectangleSpawner(val rectangle: Rectangle) : ParticleSpawner {
    override fun spawn(data: ParticleData, start: Int, end: Int) {
        val positions = data.getArray { PositionData() }

        repeat(end - start) {
            positions[it + start].x = rectangle.x + randomFloat() * rectangle.width
            positions[it + start].y = rectangle.y + randomFloat() * rectangle.height
        }
    }
}