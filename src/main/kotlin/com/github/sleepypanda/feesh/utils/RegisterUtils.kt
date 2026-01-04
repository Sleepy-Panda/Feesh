package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import net.minecraft.text.Text
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

object RegisterUtils {

    fun tick() {

    }
    
    fun chat(
        regex: Regex,
        noFormatting: Boolean = true,
        action: (message: Text, matchResult: MatchResult) -> Unit
    ) {
        ClientReceiveMessageEvents.GAME.register { message, _, ->
            var text = if (noFormatting) message.string else message.getFormattedString()
            regex.find(text)?.let { result ->
                action(message, result)
            }
        }
    }

    fun chatCancellable(
        regex: Regex,
        noFormatting: Boolean = true,
        action: (message: Text, matchResult: MatchResult) -> Boolean
    ) {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            var text = if (noFormatting) message.string else message.getFormattedString()
            regex.find(text)?.let { result ->
                return@register action(message, result)
            }
            true
        }
    }

    /**
     * Registers a command with the specified name (e.g. "feesh")
     * The action is executed when the command is invoked, with the provided arguments.
     * @param name The name of the command.
     * @param action The action to execute when the command is invoked.
     */
    fun command(name: String, action: (Array<String>) -> Unit) {
        ClientCommandRegistrationCallback.EVENT.register { registrationCallback, _ ->
            val command = ClientCommandManager.literal(name)
                .executes {
                    action(emptyArray())
                    1
                }
                .then(
                    ClientCommandManager.argument("args", StringArgumentType.greedyString())
                        .executes { context ->
                            val argsString = StringArgumentType.getString(context, "args")
                            val args = argsString.split(" ").toTypedArray()
                            action(args)
                            1
                        }
                )
                    
            registrationCallback.register(command)
        }
    }
}