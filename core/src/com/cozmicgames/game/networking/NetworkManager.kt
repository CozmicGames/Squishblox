package com.cozmicgames.game.networking

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Disposable
import com.cozmicgames.common.networking.NetworkConstants
import com.cozmicgames.common.networking.NetworkMessage
import com.cozmicgames.common.utils.Updatable
import com.cozmicgames.game.Game
import com.cozmicgames.game.tasks
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import java.net.InetAddress
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass


class NetworkManager : Updatable, Disposable {
    companion object {
        private const val CONNECTION_RETRY_DELAY = 0.5f
        private const val MAX_RECEIVE_QUEUE_TIME = 1000L * 60 // One minute
    }

    enum class ConnectionState {
        NOT_CONNECTED,
        TRYING_TO_CONNECT,
        CONNECTED,
        FAILED_CONNECTION
    }

    private val client = Client(NetworkConstants.WRITE_BUFFER_SIZE, NetworkConstants.OBJECT_BUFFER_SIZE)
    private val lock = ReentrantLock()
    private val listeners = arrayListOf<NetworkMessageListener<*>>()
    private val workingListeners = arrayListOf<NetworkMessageListener<*>>()
    private val listenersToRemove = hashSetOf<NetworkMessageListener<*>>()
    private val receiveQueue = arrayListOf<ReceivedMessage>()
    private val workingReceiveQueue = arrayListOf<ReceivedMessage>()
    private val sendQueue = arrayListOf<NetworkMessage>()
    private val workingSendQueue = arrayListOf<NetworkMessage>()
    private val messageQueues = Array(2) { arrayListOf<ReceivedMessage>() }
    private var readMessageQueue = 0
        set(value) {
            field = value % messageQueues.size
        }

    private val writeMessageQueue get() = (readMessageQueue + 1) % messageQueues.size
    private val onlineServerAddress get() = InetAddress.getByName(NetworkConstants.SERVER_ADDRESS)

    var connectionState = ConnectionState.NOT_CONNECTED
        private set

    var networkMessageHandler: NetworkMessageHandler? = null

    init {
        client.addListener(object : Listener {
            override fun received(connection: Connection, value: Any) {
                if (value is NetworkMessage)
                    lock.withLock {
                        messageQueues[writeMessageQueue] += ReceivedMessage(value, System.currentTimeMillis())
                    }
            }

            override fun connected(connection: Connection?) {
                connectionState = ConnectionState.CONNECTED
            }

            override fun disconnected(connection: Connection?) {
                connectionState = ConnectionState.NOT_CONNECTED
            }
        })

        client.kryo.isRegistrationRequired = false
    }

    fun connectToServer(address: InetAddress, maxAttempts: Int, onSuccess: (Boolean) -> Unit) {
        client.start()

        Game.tasks.submitAsync({
            tryConnection(address, 0, maxAttempts, onSuccess)
        })
    }

    private fun tryConnection(address: InetAddress, attempts: Int, maxAttempts: Int, onSuccess: (Boolean) -> Unit) {
        if (client.isConnected)
            return

        connectionState = ConnectionState.TRYING_TO_CONNECT
        try {
            client.connect(NetworkConstants.TIMEOUT, address, NetworkConstants.PORT)
            onSuccess(true)
        } catch (e: Exception) {
            if (attempts >= maxAttempts) {
                connectionState = ConnectionState.FAILED_CONNECTION
                Gdx.app.log("CLIENT", "Failed to connect to the server, entering offline mode.")
                onSuccess(false)
            } else {
                Gdx.app.log("CLIENT", "Failed to connect to the server, retrying in ${(CONNECTION_RETRY_DELAY * 1000).toInt()} ms. (${if (attempts > 0) "${attempts + 1} attempts" else "1 attempt"})")
                Game.tasks.scheduleAsync(CONNECTION_RETRY_DELAY) { tryConnection(address, attempts + 1, maxAttempts, onSuccess) }
            }
        }
    }

    inline fun <reified T : NetworkMessage> listenFor(timeout: Float = 30.0f, noinline block: (T) -> Boolean) = listenFor(T::class, timeout, block)

    fun <T : NetworkMessage> listenFor(type: KClass<T>, timeout: Float, block: (T) -> Boolean) {
        listeners += NetworkMessageListener(type, timeout, System.currentTimeMillis(), block)
    }

    fun send(message: NetworkMessage) {
        sendQueue += message
    }

    override fun update(delta: Float) {
        if (!client.isConnected) {
            if (connectionState == ConnectionState.FAILED_CONNECTION)
                sendQueue.clear()

            return
        }

        workingSendQueue.clear()
        workingSendQueue.addAll(sendQueue)
        sendQueue.clear()

        workingSendQueue.forEach {
            client.sendTCP(it)
        }

        lock.withLock {
            readMessageQueue++
        }

        messageQueues[readMessageQueue].forEach(receiveQueue::add)
        messageQueues[readMessageQueue].clear()

        workingReceiveQueue.clear()
        workingReceiveQueue.addAll(receiveQueue)

        for (message in workingReceiveQueue) {
            var isHandled = false

            workingListeners.clear()
            workingListeners.addAll(listeners)
            workingListeners.forEach { listener ->
                if (listener.type.isInstance(message.message)) {
                    if ((listener as NetworkMessageListener<NetworkMessage>).block(message.message))
                        receiveQueue.remove(message)
                    listenersToRemove += listener
                    isHandled = true
                }
            }

            if (isHandled)
                continue

            if (networkMessageHandler?.process(message.message) == true) {
                receiveQueue.remove(message)
                continue
            }

            if (System.currentTimeMillis() - MAX_RECEIVE_QUEUE_TIME > message.time)
                receiveQueue.remove(message)
        }

        listeners.forEach {
            if ((System.currentTimeMillis() - it.creationTime) / 1000.0f > it.timeout)
                listenersToRemove += it
        }

        listenersToRemove.forEach {
            listeners.remove(it)
        }
    }

    fun reconnectToOnlineServer() {
        client.reconnect()
    }

    fun disconnectFromOnlineServer() {
        client.stop()
    }

    override fun dispose() {
        disconnectFromOnlineServer()
        client.dispose()
    }
}
