package com.cozmicgames.game.graphics.engine.graphics2d.particles.generators

import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData
import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleGenerator
import com.cozmicgames.game.graphics.engine.graphics2d.particles.data.TextureData

class TextureGenerator(var texture: String) : ParticleGenerator {
    override fun generate(data: ParticleData, start: Int, end: Int) {
        val textures = data.getArray { TextureData() }

        repeat(end - start) {
            textures[it + start].texture = texture
        }
    }
}