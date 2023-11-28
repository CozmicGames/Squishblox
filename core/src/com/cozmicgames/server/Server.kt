package com.cozmicgames.server

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.cozmicgames.server.commands.CommandManager
import com.cozmicgames.server.networking.NetworkManager
import com.cozmicgames.common.utils.*
import com.cozmicgames.server.data.DataManager
import com.cozmicgames.server.networking.NetworkMessageHandler
import com.cozmicgames.server.utils.NonBlockingBufferedReader
import com.modernmt.text.profanity.ProfanityFilter
import kotlin.concurrent.thread

class Server : ApplicationAdapter() {
    companion object {
        val context = Context()

        private var shouldStop = false

        fun stop() {
            shouldStop = true
        }
    }

    private val commandThread = thread(false, true) {
        val reader = NonBlockingBufferedReader(System.`in`)
        while (!isStopped) {
            val line = reader.readLine()
            if (line != null)
                if (line.startsWith("/")) {
                    if (!commands.processFromTerminal(line.removePrefix("/")))
                        break
                }
        }
        reader.close()
    }

    private var isStopped = false
    private val handler = NetworkMessageHandler()

    override fun create() {
        Gdx.graphics.setForegroundFPS(10)
        commandThread.start()
    }

    override fun render() {
        if (shouldStop) {
            if (!isStopped) {
                Gdx.app.log("SERVER", "Stopping server.")
                Gdx.app.exit()
                isStopped = true
            }
            return
        }

        context.forEach {
            if (it is Updatable)
                it.update(Gdx.graphics.deltaTime)
        }

        networking.processMessages {
            handler.process(it)
        }
    }

    override fun dispose() {
        context.dispose()
    }
}

val Server.Companion.time by Server.context.injector { Time() }
val Server.Companion.tasks by Server.context.injector { TaskManager() }
val Server.Companion.networking by Server.context.injector { NetworkManager() }
val Server.Companion.dataRoot by Server.context.injector { Gdx.files.local("data/") }
val Server.Companion.data by Server.context.injector { DataManager() }
val Server.Companion.commands by Server.context.injector { CommandManager() }
val Server.Companion.profanityFilter by Server.context.injector { ProfanityFilter() }
