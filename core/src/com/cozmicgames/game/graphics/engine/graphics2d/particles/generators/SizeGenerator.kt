package com.cozmicgames.game.graphics.engine.graphics2d.particles.generators

import com.cozmicgames.common.utils.maths.randomFloat
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleGenerator
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.SizeData

class SizeGenerator(var minStartSize: Float, val maxStartSize: Float, var minEndSize: Float, val maxEndSize: Float) : ParticleGenerator {
    override fun generate(data: ParticleData, start: Int, end: Int) {
        val sizes = data.getArray { SizeData() }

        repeat(end - start) {
            with(sizes[it + start]) {
                startSize = minStartSize + randomFloat() * (maxStartSize - minStartSize)
                endSize = minEndSize + randomFloat() * (maxEndSize - minEndSize)
                size = startSize
            }
        }
    }
}