package com.cozmicgames.game.physics

import com.badlogic.gdx.math.Vector2

class RaycastResult(val ray: Ray, val distance: Float, val normal: Vector2, val body: Body) {
    val impact = Vector2(ray.origin.x + ray.direction.x * distance, ray.origin.y + ray.direction.y * distance)
}