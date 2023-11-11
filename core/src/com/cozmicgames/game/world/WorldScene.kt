package com.cozmicgames.game.world

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.scene.Scene

class WorldScene : Scene() {
    private val world = World()
    private var isInBlockCreationMode = false

    private val blockEditProcessor = BlockEditProcessor(world)
    private val blockCreateProcessor = BlockPreviewProcessor(this)
    private val blockPreviewRenderProcessor = BlockPreviewRenderProcessor()

    init {
        addSceneProcessor(BlockRenderProcessor())
        addSceneProcessor(blockEditProcessor)

        addGameObject {
            addComponent<BlockPreviewComponent> {
                world = this@WorldScene.world
            }
        }
    }

    fun addBlock(minX: Int, minY: Int, maxX: Int, maxY: Int, color: Color) {
        addGameObject {
            addComponent<WorldBlockComponent> {
                world = this@WorldScene.world
                this.color.set(color)
                this.minX = WorldUtils.toWorldCoord(minX)
                this.minY = WorldUtils.toWorldCoord(minY)
                this.maxX = WorldUtils.toWorldCoord(maxX)
                this.maxY = WorldUtils.toWorldCoord(maxY)
            }
        }
    }

    fun updateEditState() {
        if (Game.input.isKeyJustDown(Input.Keys.SPACE)) {
            isInBlockCreationMode = !isInBlockCreationMode

            if (isInBlockCreationMode) {
                addSceneProcessor(blockCreateProcessor)
                addSceneProcessor(blockPreviewRenderProcessor)
                removeSceneProcessor(blockEditProcessor)
            } else {
                addSceneProcessor(blockEditProcessor)
                removeSceneProcessor(blockCreateProcessor)
                removeSceneProcessor(blockPreviewRenderProcessor)
            }
        }
    }
}