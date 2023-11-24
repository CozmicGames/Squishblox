package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.scene.Scene
import com.cozmicgames.game.scene.findGameObjectByComponent
import com.cozmicgames.game.utils.Properties
import com.cozmicgames.game.world.processors.*

class WorldScene : Scene() {
    enum class EditState {
        CREATE,
        EDIT,
        DELETE,
        EDIT_PLATFORM,
        EDIT_PLAYER
    }

    val world = World()

    var editState = EditState.CREATE

    private var isFirstInitialize = true

    private fun addSceneProcessors() {
        if (!isFirstInitialize)
            return

        addSceneProcessor(BlockRenderProcessor())
        addSceneProcessor(PlatformRenderProcessor(this))
        addSceneProcessor(DebugRenderProcessor())

        addSceneProcessor(PlayerBlockEditProcessor(this))
        addSceneProcessor(BlockCreateProcessor(this))
        addSceneProcessor(BlockEditProcessor(this))
        addSceneProcessor(BlockDeleteProcessor(this))
        addSceneProcessor(PlatformEditProcessor(this))

        addSceneProcessor(PlatformMoveProcessor(this))
        addSceneProcessor(PlayerBlockProcessor(this))

        isFirstInitialize = false
    }

    fun initialize(data: String) {
        addSceneProcessors()

        val properties = Properties()
        properties.read(data)
        read(properties)
    }

    fun initialize() {
        addSceneProcessors()

        addBlock(0, 0, 3, 1, WorldUtils.getRandomBlockColor())

        addGameObject {
            addComponent<PlayerBlock> {
                this.color.set(WorldConstants.PLAYER_COLOR)
                this.minX = WorldUtils.toWorldCoord(1)
                this.minY = WorldUtils.toWorldCoord(1)
                this.maxX = WorldUtils.toWorldCoord(2)
                this.maxY = WorldUtils.toWorldCoord(3)
            }
        }

        addBlock(20, 0, 23, 1, WorldUtils.getRandomBlockColor())

        addGameObject {
            addComponent<GoalBlock> {
                this.color.set(WorldConstants.GOAL_COLOR)
                this.minX = WorldUtils.toWorldCoord(21)
                this.minY = WorldUtils.toWorldCoord(1)
                this.maxX = WorldUtils.toWorldCoord(22)
                this.maxY = WorldUtils.toWorldCoord(3)
            }
        }
    }

    fun addBlock(minX: Int, minY: Int, maxX: Int, maxY: Int, color: Color) {
        addGameObject {
            addComponent<WorldBlock> {
                this.color.set(color)
                this.minX = WorldUtils.toWorldCoord(minX)
                this.minY = WorldUtils.toWorldCoord(minY)
                this.maxX = WorldUtils.toWorldCoord(maxX)
                this.maxY = WorldUtils.toWorldCoord(maxY)
            }
        }
    }

    fun spawnPlayer(x: Int, y: Int) {
        addGameObject {
            addComponent<PlayerBlock> {
                this.color.set(Color.WHITE)
                this.minX = WorldUtils.toWorldCoord(x)
                this.minY = WorldUtils.toWorldCoord(y)
                this.maxX = WorldUtils.toWorldCoord(x + 1)
                this.maxY = WorldUtils.toWorldCoord(y + 2)
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