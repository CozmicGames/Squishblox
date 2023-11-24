package com.cozmicgames.game.physics

import com.cozmicgames.game.Game
import com.cozmicgames.game.physics
import com.cozmicgames.game.world.WorldUtils

class PlatformerController(var body: Body) {
    var jumpForce = 650.0f
    var crouchSpeedFactor = 0.5f
    var movementSpeed = 400.0f
    var movementAcceleration = 1000.0f
    var movementDeceleration = 4000.0f
    var airDeceleration = 250.0f
    var doAllowAirControl = true
    var airControlSpeedFactor = 1.0f
    var gravityFactor = 1.15f
    var maxJumpTime = 0.2f

    var isOnGround = false
        private set

    private var jumpTime = 0.0f

    fun move(amount: Float, crouch: Boolean, jump: Boolean, delta: Float) {
        if (!isOnGround)
            body.velocity.x = WorldUtils.approach(body.velocity.x, 0.0f, airDeceleration * delta)

        if (isOnGround || doAllowAirControl) {
            if (amount > 0.0f)
                body.velocity.x = WorldUtils.approach(body.velocity.x, movementSpeed, movementAcceleration * delta)
            else if (amount < 0.0f)
                body.velocity.x = WorldUtils.approach(body.velocity.x, -movementSpeed, -movementAcceleration * delta)
            else
                body.velocity.x = WorldUtils.approach(body.velocity.x, 0.0f, movementDeceleration * delta)

            if (crouch)
                body.velocity.x *= crouchSpeedFactor

            if (!isOnGround)
                body.velocity.x *= airControlSpeedFactor
        }


        if (!isOnGround && body.velocity.y < 0.0f)
            body.velocity.y *= gravityFactor

        if (jump && jumpTime < maxJumpTime)
            body.velocity.y += jumpForce * (maxJumpTime - jumpTime)

        if (!isOnGround)
            jumpTime += delta

        val wasOnGround = isOnGround
        isOnGround = false

        Game.physics.forEachOverlappingRectangle(body.bounds.x, body.bounds.y - 0.1f, body.bounds.width, 0.1f) {
            if (it != body)
                isOnGround = true
        }

        if (!wasOnGround && isOnGround)
            jumpTime = 0.0f
    }
}