package com.cozmicgames.game.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.cozmicgames.game.Game
import com.cozmicgames.game.input
import com.cozmicgames.game.player
import com.cozmicgames.game.scene.Component
import com.cozmicgames.game.scene.components.TransformComponent
import com.cozmicgames.game.utils.Updatable
import kotlin.math.max
import kotlin.math.round

sealed class BlockComponent : Component() {
    private val transformComponent by lazy { gameObject.getOrAddComponent<TransformComponent>() }

    val color = Color()

    var x
        get() = transformComponent.transform.x
        set(value) {
            transformComponent.transform.x = value
        }

    var y
        get() = transformComponent.transform.y
        set(value) {
            transformComponent.transform.y = value
        }

    var width
        get() = transformComponent.transform.scaleX
        set(value) {
            transformComponent.transform.scaleX = value
        }

    var height
        get() = transformComponent.transform.scaleY
        set(value) {
            transformComponent.transform.scaleY = value
        }
}

class WorldBlockComponent : BlockComponent(), Updatable {
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var isDragged = false
    private var isResizingFlag = 0

    private var isFirstUpdate = true

    override fun update(delta: Float) {
        if (isFirstUpdate) {
            isFirstUpdate = false
            return
        }

        edit()
    }

    fun edit() {
        val isTopLeftHovered = Game.player.isHovered(x, y, WorldConstants.RESIZE_BORDER_SIZE, WorldConstants.RESIZE_BORDER_SIZE)
        val isTopCenterHovered = Game.player.isHovered(x + WorldConstants.RESIZE_BORDER_SIZE, y, width - WorldConstants.RESIZE_BORDER_SIZE * 2, WorldConstants.RESIZE_BORDER_SIZE)
        val isTopRightHovered = Game.player.isHovered(x + width - WorldConstants.RESIZE_BORDER_SIZE, y, WorldConstants.RESIZE_BORDER_SIZE, WorldConstants.RESIZE_BORDER_SIZE)

        val isCenterLeftHovered = Game.player.isHovered(x, y + WorldConstants.RESIZE_BORDER_SIZE, WorldConstants.RESIZE_BORDER_SIZE, height - WorldConstants.RESIZE_BORDER_SIZE * 2)
        val isCenterRightHovered = Game.player.isHovered(x + width - WorldConstants.RESIZE_BORDER_SIZE, y + WorldConstants.RESIZE_BORDER_SIZE, WorldConstants.RESIZE_BORDER_SIZE, height - WorldConstants.RESIZE_BORDER_SIZE * 2)

        val isBottomLeftHovered = Game.player.isHovered(x, y + height - WorldConstants.RESIZE_BORDER_SIZE, WorldConstants.RESIZE_BORDER_SIZE, WorldConstants.RESIZE_BORDER_SIZE)
        val isBottomCenterHovered = Game.player.isHovered(x + WorldConstants.RESIZE_BORDER_SIZE, y + height - WorldConstants.RESIZE_BORDER_SIZE, width - WorldConstants.RESIZE_BORDER_SIZE * 2, WorldConstants.RESIZE_BORDER_SIZE)
        val isBottomRightHovered = Game.player.isHovered(x + width - WorldConstants.RESIZE_BORDER_SIZE, y + height - WorldConstants.RESIZE_BORDER_SIZE, WorldConstants.RESIZE_BORDER_SIZE, WorldConstants.RESIZE_BORDER_SIZE)

        val isCenterHovered = Game.player.isHovered(x + WorldConstants.RESIZE_BORDER_SIZE, y + WorldConstants.RESIZE_BORDER_SIZE, width - WorldConstants.RESIZE_BORDER_SIZE * 2, height - WorldConstants.RESIZE_BORDER_SIZE * 2)

        if (isResizingFlag == 0) {
            if (isTopLeftHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NWSEResize)
            else if (isTopCenterHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
            else if (isTopRightHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NESWResize)
            else if (isCenterLeftHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
            else if (isCenterRightHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
            else if (isBottomLeftHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NESWResize)
            else if (isBottomCenterHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
            else if (isBottomRightHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NWSEResize)
            else if (isCenterHovered)
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.AllResize)
            else
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
        }

        if (Game.input.justTouchedDown) {
            isResizingFlag = if (isTopLeftHovered) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NWSEResize)
                offsetX = Game.player.inputX - x
                offsetY = Game.player.inputY - y
                1 shl 0
            } else if (isTopCenterHovered) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
                offsetX = Game.player.inputX - x
                offsetY = Game.player.inputY - y
                1 shl 1
            } else if (isTopRightHovered) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NESWResize)
                offsetX = Game.player.inputX - x - width
                offsetY = Game.player.inputY - y
                1 shl 2
            } else if (isCenterLeftHovered) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
                offsetX = Game.player.inputX - x
                offsetY = Game.player.inputY - y
                1 shl 3
            } else if (isCenterRightHovered) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize)
                offsetX = Game.player.inputX - width
                offsetY = Game.player.inputY
                1 shl 4
            } else if (isBottomLeftHovered) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NESWResize)
                offsetX = Game.player.inputX
                offsetY = Game.player.inputY - height
                1 shl 5
            } else if (isBottomCenterHovered) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize)
                offsetX = Game.player.inputX
                offsetY = Game.player.inputY - height
                1 shl 6
            } else if (isBottomRightHovered) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NWSEResize)
                offsetX = Game.player.inputX - width
                offsetY = Game.player.inputY - height
                1 shl 7
            } else
                0

            if (isCenterHovered) {
                isDragged = true
                offsetX = Game.player.inputX - x
                offsetY = Game.player.inputY - y
            }
        }

        if (Game.input.justTouchedUp) {
            isDragged = false
            isResizingFlag = 0
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
        }

        if (isDragged) {
            x = round((Game.player.inputX - offsetX) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            y = round((Game.player.inputY - offsetY) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
        }

        if (!isDragged && isResizingFlag and (1 shl 0) != 0) {
            val newX = round((Game.player.inputX - offsetX) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            val newY = round((Game.player.inputY - offsetY) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            width -= newX - x
            height -= newY - y
            x = newX
            y = newY
        }

        if (!isDragged && isResizingFlag and (1 shl 1) != 0) {
            val newY = round((Game.player.inputY - offsetY) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            height -= newY - y
            y = newY
        }

        if (!isDragged && isResizingFlag and (1 shl 2) != 0) {
            val newX = round((Game.player.inputX - offsetX) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            val newY = round((Game.player.inputY - offsetY) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            width = newX - x
            height -= newY - y
            y = newY
        }

        if (!isDragged && isResizingFlag and (1 shl 3) != 0) {
            val newX = round((Game.player.inputX - offsetX) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            width -= newX - x
            x = newX
        }

        if (!isDragged && isResizingFlag and (1 shl 4) != 0) {
            val newX = round((Game.player.inputX - offsetX) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            width = newX - x
        }

        if (!isDragged && isResizingFlag and (1 shl 5) != 0) {
            val newX = round((Game.player.inputX - offsetX) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            val newY = round((Game.player.inputY - offsetY) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            width -= newX - x
            height = newY - y
            x = newX
        }

        if (!isDragged && isResizingFlag and (1 shl 6) != 0) {
            val newY = round((Game.player.inputY - offsetY) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            height = newY - y
        }

        if (!isDragged && isResizingFlag and (1 shl 7) != 0) {
            val newX = round((Game.player.inputX - offsetX) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            val newY = round((Game.player.inputY - offsetY) / WorldConstants.WORLD_CELL_SIZE) * WorldConstants.WORLD_CELL_SIZE
            width = newX - x
            height = newY - y
        }

        width = max(width, WorldConstants.WORLD_CELL_SIZE)
        height = max(height, WorldConstants.WORLD_CELL_SIZE)

        //TODO: Resolve collisions
    }
}
