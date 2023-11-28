package com.cozmicgames.server.networking

import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.common.networking.NetworkConstants
import com.cozmicgames.common.networking.NetworkMessage
import com.cozmicgames.server.tasks
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class NetworkManager : Disposable {
    private val server = Server(NetworkConstants.WRITE_BUFFER_SIZE, NetworkConstants.OBJECT_BUFFER_SIZE)
    private val activeConnections = arrayListOf<Connection>()
    private val lock = ReentrantLock()
    private val messageQueues = Array(2) { arrayListOf<ReceivedMessage>() }
    private var readMessageQueue = 0
        set(value) {
            field = value % messageQueues.size
        }

    private val writeMessageQueue get() = (readMessageQueue + 1) % messageQueues.size

    init {
        server.addListener(object : Listener {
            override fun connected(connection: Connection) {
                lock.withLock {
                    activeConnections += connection
                }
            }

            override fun disconnected(connection: Connection) {
                lock.withLock {
                    activeConnections -= connection
                }
            }

            override fun received(connection: Connection, value: Any) {
                if (value is NetworkMessage)
                    lock.withLock {
                        messageQueues[writeMessageQueue] += ReceivedMessage(connection, value)
                    }
            }
        })

        server.kryo.isRegistrationRequired = false
        server.bind(NetworkConstants.PORT)
        server.start()
    }

    fun processMessages(block: (ReceivedMessage) -> Unit) {
        lock.withLock {
            readMessageQueue++
        }

        messageQueues[readMessageQueue].forEach(block)
        messageQueues[readMessageQueue].clear()
    }

    fun send(connection: Connection, message: NetworkMessage): Boolean {
        if (!connection.isConnected)
            return false
        com.cozmicgames.server.Server.tasks.submitAsync({
            connection.sendTCP(message)
        })
        return true
    }

    fun broadcast(message: NetworkMessage, filter: ((Connection) -> Boolean)? = null) {
        if (filter != null)
            activeConnections.forEach {
                if (filter(it))
                    it.sendTCP(message)
            }
        else
            server.sendToAllTCP(message)
    }

    override fun dispose() {
        server.stop()
        server.dispose()
    }
}