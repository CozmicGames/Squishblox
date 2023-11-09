package com.cozmicgames.game.world

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.scene.Scene
import com.cozmicgames.game.scene.findGameObjectByComponent

class WorldScene : Scene() {
    private val world = World()
    private var isInBlockCreationMode = false

    init {
        addSceneProcessor(BlockRenderProcessor())
        addSceneProcessor(BlockEditProcessor(world))
        addSceneProcessor(BlockPreviewProcessor(this))

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
            addComponent<PlayerBlockComponent> {
                world = this@WorldScene.world
                color.set(Color.CHARTREUSE)
                minX = -128.0f
                minY = 0.0f
                maxX = -64.0f
                maxY = 64.0f
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
        if (Game.input.isKeyJustDown(Input.Keys.SPACE))
            isInBlockCreationMode = !isInBlockCreationMode

        if (isInBlockCreationMode)
            addGameObject {
                addComponent<BlockPreviewComponent> {
                    world = this@WorldScene.world
                }
            }
        else
            findGameObjectByComponent<BlockPreviewComponent> { true }?.let {
                removeGameObject(it)
            }
    }
}