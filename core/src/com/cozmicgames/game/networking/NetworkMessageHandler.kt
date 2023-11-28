package com.cozmicgames.game.networking

import com.cozmicgames.common.networking.NetworkMessage

interface NetworkMessageHandler {
    fun process(message: NetworkMessage): Boolean
}