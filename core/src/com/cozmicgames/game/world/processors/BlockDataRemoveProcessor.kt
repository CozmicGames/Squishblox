package com.cozmicgames.game.world.processors

import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.player
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.game.world.WorldBlock
import com.cozmicgames.game.world.WorldScene
import com.cozmicgames.game.world.WorldUtils
import com.cozmicgames.game.world.dataValues.PlatformData
import com.cozmicgames.game.world.dataValues.ScaleData

class BlockDataRemoveProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.EDIT && (worldScene.editState == WorldScene.EditState.EDIT_SCALE_UP || worldScene.editState == WorldScene.EditState.EDIT_SCALE_DOWN || worldScene.editState == WorldScene.EditState.EDIT_PLATFORM)
    }

    override fun process(delta: Float) {
        if (!Game.player.isCursorPositionVisible())
            return

        val hoveredId = worldScene.world.getBlock(WorldUtils.toCellCoord(Game.player.inputX), WorldUtils.toCellCoord(Game.player.inputY))

        if (Game.input.isButtonJustDown(2))
            worldScene.findGameObjectsWithComponent<WorldBlock> {
                val block = it.getComponent<WorldBlock>()!!

                if (block.id == hoveredId) {
                    block.removeData<ScaleData>()
                    block.removeData<PlatformData>()
                }
            }
    }
}