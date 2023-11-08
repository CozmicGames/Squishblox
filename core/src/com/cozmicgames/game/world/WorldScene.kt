package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.scene.Scene

class WorldScene : Scene() {
    private val world = World()

    init {
        addSceneProcessor(BlockRenderProcessor())
        addSceneProcessor(BlockEditProcessor(world))

        addGameObject {
            addComponent<WorldBlockComponent> {
                world = this@WorldScene.world
                color.set(Color.LIME)
                minX = 0.0f
                minY = 0.0f
                maxX = 64.0f
                maxY = 64.0f
            }
        }

        addGameObject {
            addComponent<WorldBlockComponent> {
                world = this@WorldScene.world
                color.set(Color.CHARTREUSE)
                minX = -128.0f
                minY = 0.0f
                maxX = -64.0f
                maxY = 64.0f
            }
        }
    }
}