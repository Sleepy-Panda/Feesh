package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.mojang.brigadier.arguments.StringArgumentType
//#if MC >= 26.1
//$$ import net.fabricmc.fabric.api.client.command.v2.ClientCommands as ClientCommandManager
//#else
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
//#endif
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component

object RegisterUtils {

    fun tick() {

    }
    
    /*
     * Registers a chat message listener with the specified regex.
     * @param regex The regex to match the chat message.
     * @param noFormatting Whether to remove formatting codes from the chat message. Then the pattern should be without formatting codes.
     * @param action The action to execute when the chat message is matched.
     */
    fun chat(
        regex: Regex,
        noFormatting: Boolean = true,
        action: (message: Component, matchResult: MatchResult) -> Unit
    ) {
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            var text = if (noFormatting) message.string.removeFormatting() else message.getFormattedString()
            regex.find(text)?.let { result ->
                action(message, result)
            }
        }
    }

    /*
     * Registers a chat message listener with the specified regex. This lets you cancel the chat message using the return value.
     * @param regex The regex to match the chat message.
     * @param noFormatting Whether to remove formatting codes from the chat message. Then the pattern should be without formatting codes.
     * @param action The action to execute when the chat message is matched.
     */
    fun chatCancellable(
        regex: Regex,
        noFormatting: Boolean = true,
        action: (message: Component, matchResult: MatchResult) -> Boolean
    ) {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            var text = if (noFormatting) message.string.removeFormatting() else message.getFormattedString()
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