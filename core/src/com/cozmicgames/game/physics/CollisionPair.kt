package com.cozmicgames.game.physics

class CollisionPair(val a: Body, val b: Body) {
    var shouldCollide = false

    lateinit var manifold: Manifold
}