package com.cozmicgames.game.world

import com.badlogic.gdx.graphics.Color
import com.cozmicgames.game.scene.Scene
import com.cozmicgames.game.scene.findGameObjectByComponent
import com.cozmicgames.game.world.processors.*

class WorldScene : Scene() {
    enum class EditState {
        CREATE,
        EDIT,
        DELETE,
        PLATFORM
    }

    val world = World()
    val physicsWorld = PhysicsWorld()

    var editState = EditState.CREATE

    init {
        addSceneProcessor(BlockRenderProcessor())
        addSceneProcessor(PlatformRenderProcessor(this))

        addSceneProcessor(BlockCreateProcessor(this))
        addSceneProcessor(BlockEditProcessor(this))
        addSceneProcessor(BlockDeleteProcessor(this))
        addSceneProcessor(PlatformEditProcessor(this))

        addSceneProcessor(PlatformMoveProcessor(this))
        addSceneProcessor(PlayerBlockProcessor(this))
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
            physicsWorld.removeBlock(it.id)
            removeGameObject(it.gameObject)
        }
    }
}