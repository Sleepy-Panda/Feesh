package com.github.sleepypanda.feesh.utils

import net.minecraft.text.Text
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

object RegisterUtils {

    fun chat(
        regex: Regex,
        action: (message: Text, matchResult: MatchResult) -> Unit
    ) {
        ClientReceiveMessageEvents.GAME.register { message, _, ->
            var text = message.string
            regex.find(text)?.let { result ->
                action(message, result)
            }
        }
    }

    fun chatCancellable(
        regex: Regex,
        action: (message: Text, matchResult: MatchResult) -> Boolean
    ) {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            var text = message.string
            regex.find(text)?.let { result ->
                return@register action(message, result)
            }
            true
        }
    }

    /**
     * Registers a command with the specified name and aliases.
     * The action is executed when the command is invoked, with the provided arguments.
     *
     * @param name The name of the command.
     * @param aliases Optional aliases for the command.
     * @param action The action to execute when the command is invoked.
     */
    fun command(
        name: String,
        vararg aliases: String,
        action: (Array<String>) -> Unit
    ) {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->

            fun createLiteral(commandName: String) =
                ClientCommandManager.literal(commandName)
                    .executes {
                        action(emptyArray())
                        1
                    }
                    .then(
                        ClientCommandManager.argument("args", StringArgumentType.greedyString())
                            .executes {
                                val argsString = StringArgumentType.getString(it, "args")
                                val args = argsString.split(' ').filter { s -> s.isNotEmpty() }.toTypedArray()
                                action(args)
                                1
                            }
                    )

            dispatcher.register(createLiteral(name))

            aliases.forEach { alias ->
                dispatcher.register(createLiteral(alias))
            }
        }
    }
}