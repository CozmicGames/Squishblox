package com.cozmicgames.game.networking

import com.cozmicgames.common.networking.NetworkMessage
import kotlin.reflect.KClass

class NetworkMessageListener<T : NetworkMessage>(val type: KClass<T>, val timeout: Float, val creationTime: Long, val block: (T) -> Boolean)