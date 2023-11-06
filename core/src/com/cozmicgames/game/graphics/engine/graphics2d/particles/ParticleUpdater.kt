package com.cozmicgames.game.graphics.engine.graphics2d.particles

interface ParticleUpdater {
    fun init(data: ParticleData)
    fun update(data: ParticleData, delta: Float)
}