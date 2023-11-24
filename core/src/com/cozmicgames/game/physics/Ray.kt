package com.cozmicgames.game.physics

import com.badlogic.gdx.math.Vector2

data class Ray(val origin: Vector2, val direction: Vector2, var length: Float = 0.0f)