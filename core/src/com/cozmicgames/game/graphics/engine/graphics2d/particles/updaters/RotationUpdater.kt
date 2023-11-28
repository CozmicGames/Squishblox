package com.cozmicgames.game.graphics.engine.graphics2d.particles.updaters

import com.cozmicgames.common.utils.maths.lerp
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleUpdater
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.AngleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.TimeData

class RotationUpdater : ParticleUpdater {
    private lateinit var angles: Array<AngleData>
    private lateinit var times: Array<TimeData>

    override fun init(data: ParticleData) {
        angles = data.getArray { AngleData() }
        times = data.getArray { TimeData() }
    }

    override fun update(data: ParticleData, delta: Float) {
        repeat(data.numberOfAlive) {
            angles[it].angle = lerp(angles[it].startAngle, angles[it].endAngle, times[it].interpolationValue)
        }
    }
}