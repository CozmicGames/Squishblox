package com.cozmicgames.game.physics

interface CollisionListener {
    fun onCollision(collisionPair: CollisionPair)
}