package com.cozmicgames.server.networking

import com.cozmicgames.common.networking.NetworkMessage
import com.esotericsoftware.kryonet.Connection

class ReceivedMessage(val connection: Connection, val message: NetworkMessage)