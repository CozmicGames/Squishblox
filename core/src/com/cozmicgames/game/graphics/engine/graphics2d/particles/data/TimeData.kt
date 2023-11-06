package com.cozmicgames.game.graphics.engine.graphics2d.particles.data

import com.cozmicgames.game.graphics.engine.graphics2d.particles.ParticleData

data class TimeData(var remainingLifeTime: Float=0.0f, var lifeTime: Float=0.0f, var interpolationValue: Float = 0.0f) : ParticleData.DataType

