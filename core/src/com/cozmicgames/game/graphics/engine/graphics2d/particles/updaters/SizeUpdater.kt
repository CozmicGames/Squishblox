package com.cozmicgames.game.graphics.engine.graphics2d.particles.updaters

import com.cozmicgames.common.utils.maths.lerp
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleUpdater
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.SizeData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.TimeData

class SizeUpdater : ParticleUpdater {
    private lateinit var sizes: Array<SizeData>
    private lateinit var times: Array<TimeData>

    override fun init(data: ParticleData) {
        sizes = data.getArray { SizeData() }
        times = data.getArray { TimeData() }
    }

    override fun update(data: ParticleData, delta: Float) {
        repeat(data.numberOfAlive) {
            sizes[it].size = lerp(sizes[it].startSize, sizes[it].endSize, times[it].interpolationValue)
        }
    }
}