package com.cozmicgames.server.commands

import com.badlogic.gdx.Gdx
import com.cozmicgames.server.Server
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.exceptions.CommandSyntaxException

class CommandManager {
    private sealed interface CommandSource {
        object TerminalSource : CommandSource {
            override fun respond(message: String) {
                Gdx.app.log("SERVER", message)
            }
        }

        fun respond(message: String)
    }

    private val dispatcher = CommandDispatcher<CommandSource>()

    init {
        dispatcher.register(
            literal<CommandSource>("stop").executes {
                Server.stop()
                0
            }
        )
    }

    fun processFromTerminal(command: String): Boolean {
        try {
            return dispatcher.execute(command, CommandSource.TerminalSource) > 0
        } catch (e: CommandSyntaxException) {
            Gdx.app.log("SERVER", "Failed to execute command from terminal: $command")
        }
        return true
    }
}
