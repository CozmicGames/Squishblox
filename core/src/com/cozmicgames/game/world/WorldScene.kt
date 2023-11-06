package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.scene.Scene

class WorldScene : Scene() {
    init {
        addSceneProcessor(BlockRenderProcessor())

        addGameObject {
            addComponent<WorldBlockComponent> {
                color.set(Color.LIME)
                x = 0.0f
                y = 0.0f
                width = 64.0f
                height = 64.0f
            }
        }
    }
}