package com.github.sleepypanda.feesh.utils

import com.mojang.brigadier.arguments.StringArgumentType
//#if MC >= 26.1
//$$ import net.fabricmc.fabric.api.client.command.v2.ClientCommands as ClientCommandManager
//#else
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
//#endif
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object RegisterUtils {

    fun tick() {

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