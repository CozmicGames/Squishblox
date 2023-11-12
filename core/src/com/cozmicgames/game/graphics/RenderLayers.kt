package com.cozmicgames.game.graphics

object RenderLayers {
    const val GUI_BASE_LAYER = 1000

    const val WORLD_LAYER_BEGIN = 10
    const val WORLD_LAYER_END = WORLD_LAYER_BEGIN + 100
    const val WORLD_LAYER_BACKGROUND = WORLD_LAYER_BEGIN
    const val WORLD_LAYER_BLOCK_SHADOW = WORLD_LAYER_BEGIN + 1
    const val WORLD_LAYER_BLOCK = WORLD_LAYER_BEGIN + 2
    const val WORLD_LAYER_BLOCK_PREVIEW = WORLD_LAYER_BEGIN + 3
    const val WORLD_LAYER_CLOUD_SHADOW = WORLD_LAYER_BEGIN + 4
    const val WORLD_LAYER_CLOUD = WORLD_LAYER_BEGIN + 5
}