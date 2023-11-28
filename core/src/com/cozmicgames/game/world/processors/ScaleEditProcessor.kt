package com.cozmicgames.game.world.processors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.cozmicgames.game.*
import com.cozmicgames.game.graphics.RenderLayers
import com.cozmicgames.game.graphics.engine.graphics2d.NinepatchRenderable2D
import com.cozmicgames.game.player.PlayState
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent
import com.cozmicgames.game.world.WorldBlock
import com.cozmicgames.game.world.WorldConstants
import com.cozmicgames.game.world.WorldScene
import com.cozmicgames.game.world.WorldUtils
import com.cozmicgames.game.world.dataValues.PlatformData
import com.cozmicgames.game.world.dataValues.ScaleData

class ScaleEditProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    private val blockPreviewNinePatch: NinePatch

    init {
        val blockPreviewTexture = TextureRegion(Game.textures.getTexture("textures/block_preview.png"))
        blockPreviewNinePatch = NinePatch(blockPreviewTexture, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionWidth / 3, blockPreviewTexture.regionHeight / 3, blockPreviewTexture.regionHeight / 3)
    }

    override fun shouldProcess(delta: Float): Boolean {
        return Game.player.playState == PlayState.EDIT && (worldScene.editState == WorldScene.EditState.EDIT_SCALE_UP || worldScene.editState == WorldScene.EditState.EDIT_SCALE_DOWN)
    }

    private fun edit(block: WorldBlock) {
        if (Game.input.isButtonJustDown(0)) {
            block.removeData<PlatformData>()
            val data = ScaleData()
            data.scale = if (worldScene.editState == WorldScene.EditState.EDIT_SCALE_UP) WorldConstants.SCALE_AMOUNT else -WorldConstants.SCALE_AMOUNT
            block.setData(data)
        }
    }

    override fun process(delta: Float) {
        if (!Game.player.isCursorPositionVisible())
            return

        val hoveredId = worldScene.world.getBlock(WorldUtils.toCellCoord(Game.player.inputX), WorldUtils.toCellCoord(Game.player.inputY))

        if (hoveredId != null) {
            worldScene.findGameObjectsWithComponent<WorldBlock> {
                val block = it.getComponent<WorldBlock>()!!

                if (block.id == hoveredId) {
                    Game.graphics2d.submit<NinepatchRenderable2D> {
                        it.layer = RenderLayers.WORLD_LAYER_BLOCK_PREVIEW
                        it.ninePatch = blockPreviewNinePatch
                        it.color = Color.WHITE
                        it.x = block.minX
                        it.y = block.minY
                        it.width = block.maxX - block.minX
                        it.height = block.maxY - block.minY
                    }

                    edit(block)
                }
            }
        } else
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }
}