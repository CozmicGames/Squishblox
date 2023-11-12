package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.scene.Scene
import com.cozmicgames.game.scene.findGameObjectByComponent

class WorldScene : Scene() {
    enum class EditState {
        CREATE,
        EDIT,
        DELETE
    }

    val world = World()

    var editState = EditState.CREATE
        private set

    private val blockEditProcessor = BlockEditProcessor(this)
    private val blockCreateProcessor = BlockCreateProcessor(this)
    private val blockDeleteProcessor = BlockDeleteProcessor(this)

    init {
        addSceneProcessor(BlockRenderProcessor())
        addSceneProcessor(BlockPreviewRenderProcessor(this))
        addSceneProcessor(blockCreateProcessor)
        addSceneProcessor(blockEditProcessor)
        addSceneProcessor(blockDeleteProcessor)

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

    fun getBlockFromId(id: Int): BlockComponent? = findGameObjectByComponent<BlockComponent> {
        it.getComponent<BlockComponent>()?.id == id
    }?.getComponent()

    fun removeBlock(id: Int) {
        getBlockFromId(id)?.let {
            world.removeBlock(it.id)
            removeGameObject(it.gameObject)
        }
    }
}