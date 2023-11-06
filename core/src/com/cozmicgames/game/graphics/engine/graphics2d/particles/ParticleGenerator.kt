package com.cozmicgames.game.graphics.engine.graphics2d.particles

interface ParticleGenerator {
    fun generate(data: ParticleData, start: Int, end: Int)
}