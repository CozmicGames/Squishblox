package com.cozmicgames.game.graphics.engine.graphics2d.particles

interface ParticleSpawner {
    fun spawn(data: ParticleData, start: Int, end: Int)
}