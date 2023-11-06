package com.cozmicgames.game.graphics.engine.graphics2d.particles.updaters

import com.badlogic.gdx.math.Vector2
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleUpdater
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.AccelerationData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.PositionData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.VelocityData

class MovementUpdater(val globalAcceleration: Vector2 = Vector2.Zero) : ParticleUpdater {
    private lateinit var positions: Array<PositionData>
    private lateinit var accelerations: Array<AccelerationData>
    private lateinit var velocities: Array<VelocityData>

    override fun init(data: ParticleData) {
        positions = data.getArray { PositionData() }
        accelerations = data.getArray { AccelerationData() }
        velocities = data.getArray { VelocityData() }
    }

    override fun update(data: ParticleData, delta: Float) {
        repeat(data.numberOfAlive) {
            accelerations[it].x += globalAcceleration.x
            accelerations[it].y += globalAcceleration.y
            positions[it].x += delta * velocities[it].x
            positions[it].y += delta * velocities[it].y
            velocities[it].x += delta * accelerations[it].x
            velocities[it].y += delta * accelerations[it].y
            accelerations[it].x = 0.0f
            accelerations[it].y = 0.0f
        }
    }
}