package com.cozmicgames.game.world.processors

import com.badlogic.gdx.Input.Keys
import com.cozmicgames.game.*
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectByComponent
import com.cozmicgames.game.world.*
import com.cozmicgames.game.world.dataValues.ScaleData
import kotlin.math.abs

class PlayerBlockProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    var jumpForce = 625.0f
    var crouchSpeedFactor = 0.8f
    var movementSpeed = 400.0f
    var movementAcceleration = 1000.0f
    var movementDeceleration = 4000.0f
    var airDeceleration = 250.0f
    var doAllowAirControl = true
    var airControlSpeedFactor = 1.0f
    var gravityFactor = 1.125f
    var maxJumpTime = 0.2f

    var isOnGround = false
        private set

    private var jumpTime = 0.0f

    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState != PlayState.EDIT
    }

    override fun process(delta: Float) {
        val playerBlock = worldScene.findGameObjectByComponent<PlayerBlock> { true }?.getComponent<PlayerBlock>() ?: return

        val left = Game.input.isKeyDown(Keys.A)
        val right = Game.input.isKeyDown(Keys.D)
        val jump = Game.input.isKeyDown(Keys.SPACE)
        val crouch = Game.input.isKeyDown(Keys.SHIFT_LEFT) || Game.input.isKeyDown(Keys.SHIFT_RIGHT)
        val jumpJustPressed = Game.input.isKeyJustDown(Keys.SPACE)
        val jumpJustReleased = Game.input.isKeyJustUp(Keys.SPACE)

        var moveAmount = 0.0f

        if (left)
            moveAmount -= 1.0f

        if (right)
            moveAmount += 1.0f

        if (abs(moveAmount) > 0.0f)
            playerBlock.isFacingRight = moveAmount > 0.0f


        if (!isOnGround)
            playerBlock.body.velocity.x = WorldUtils.approach(playerBlock.body.velocity.x, 0.0f, airDeceleration * delta)

        if (isOnGround || doAllowAirControl) {
            if (moveAmount > 0.0f)
                playerBlock.body.velocity.x = WorldUtils.approach(playerBlock.body.velocity.x, movementSpeed, movementAcceleration * delta)
            else if (moveAmount < 0.0f)
                playerBlock.body.velocity.x = WorldUtils.approach(playerBlock.body.velocity.x, -movementSpeed, -movementAcceleration * delta)
            else
                playerBlock.body.velocity.x = WorldUtils.approach(playerBlock.body.velocity.x, 0.0f, movementDeceleration * delta)

            if (crouch)
                playerBlock.body.velocity.x *= crouchSpeedFactor

            if (!isOnGround)
                playerBlock.body.velocity.x *= airControlSpeedFactor
        }

        if (!isOnGround && playerBlock.body.velocity.y < 0.0f)
            playerBlock.body.velocity.y *= gravityFactor

        if (jump && jumpTime < maxJumpTime) {
            if (playerBlock.body.velocity.y <= 0.0f)
                Game.audio.playSound("sounds/jump.wav")

            playerBlock.body.velocity.y += playerBlock.calculateJumpForce(jumpForce) * (maxJumpTime - jumpTime)
        }

        if (!isOnGround)
            jumpTime += delta

        val wasOnGround = isOnGround
        isOnGround = false

        Game.physics.forEachOverlappingRectangle(playerBlock.body.bounds.x - 4.0f, playerBlock.body.bounds.y - 1.0f, playerBlock.body.bounds.width - 8.0f, 1.0f) {
            if (it != playerBlock.body)
                isOnGround = true

            (it.userData as? WorldBlock)?.getData<ScaleData>()?.let {
                playerBlock.scale(it.scale * delta)
            }
        }

        if (!wasOnGround && isOnGround)
            jumpTime = 0.0f
    }
}