package com.cozmicgames.game.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.player
import com.cozmicgames.game.scene.SceneProcessor
import com.cozmicgames.game.scene.findGameObjectsWithComponent

class BlockDeleteProcessor(private val worldScene: WorldScene) : SceneProcessor() {
    override fun shouldProcess(delta: Float): Boolean {
        return worldScene.editState == WorldScene.EditState.DELETE
    }

    override fun process(delta: Float) {
        val scene = this.scene as? WorldScene ?: return

        val hoveredId = worldScene.world.getBlock(WorldUtils.toCellCoord(Game.player.inputX), WorldUtils.toCellCoord(Game.player.inputY))

        if (hoveredId != null) {
            scene.findGameObjectsWithComponent<WorldBlockComponent> {
                val blockComponent = it.getComponent<WorldBlockComponent>()!!
                if (blockComponent.id == hoveredId && Game.input.isButtonJustDown(0))
                    scene.removeBlock(hoveredId)
            }
        } else
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }
}