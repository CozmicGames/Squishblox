package com.cozmicgames.game.physics

interface CollisionFilter {
    fun shouldCollide(a: Fixture<*>, b: Fixture<*>): Boolean
}