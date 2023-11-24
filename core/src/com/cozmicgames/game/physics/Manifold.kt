package com.cozmicgames.game.physics

import com.badlogic.gdx.math.Vector2

class Manifold {
    lateinit var a: Body
    lateinit var b: Body

    var penetration = 0.0f
    val normal = Vector2()
    val contacts = Array(2) { Vector2() }
    var numContacts = 0

    var restitution = 0.0f
    var staticFriction = 0.0f
    var dynamicFriction = 0.0f
}