package com.cozmicgames.game.scene.processors

import com.cozmicgames.game.Game
import com.cozmicgames.game.graphics.engine.graphics2d.AnimatedSpriteRenderable2D
import com.cozmicgames.game.graphics.engine.graphics2d.StaticSpriteRenderable2D
import com.cozmicgames.game.graphics.engine.graphics2d.sprite.AnimatedSpriteMaterial
import com.cozmicgames.game.graphics.engine.graphics2d.sprite.StaticSpriteMaterial
import com.cozmicgames.game.graphics2d
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.components.SpriteAnimationControllerComponent
import com.cozmicgames.game.scene.components.SpriteComponent
import com.cozmicgames.game.scene.components.TransformComponent

class SpriteRenderProcessor : SceneProcessor() {
    override fun shouldProcess(delta: Float): Boolean {
        return true
    }

    override fun process(delta: Float) {
        val scene = scene ?: return

        for (gameObject in scene.activeGameObjects) {
            val transformComponent = gameObject.getComponent<TransformComponent>() ?: continue
            val spriteComponent = gameObject.getComponent<SpriteComponent>() ?: continue

            when (spriteComponent.material) {
                is StaticSpriteMaterial -> {
                    Game.graphics2d.submit<StaticSpriteRenderable2D> {
                        it.layer = spriteComponent.layer
                        it.originX = spriteComponent.originX
                        it.originY = spriteComponent.originY
                        it.transform.set(transformComponent.transform.global)
                        it.material = spriteComponent.material as StaticSpriteMaterial
                    }
                }

                is AnimatedSpriteMaterial -> {
                    val controller = gameObject.getComponent<SpriteAnimationControllerComponent>() ?: continue
                    Game.graphics2d.submit<AnimatedSpriteRenderable2D> {
                        it.layer = spriteComponent.layer
                        it.originX = spriteComponent.originX
                        it.originY = spriteComponent.originY
                        it.transform.set(transformComponent.transform.global)
                        it.material = spriteComponent.material as AnimatedSpriteMaterial
                        it.animationStateTime = controller.animationStateTime
                    }
                }
            }
        }
    }
}