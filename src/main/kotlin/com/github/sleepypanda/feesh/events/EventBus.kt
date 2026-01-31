package com.github.sleepypanda.feesh.events

import kotlin.reflect.KClass
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.text.Text
import net.minecraft.client.MinecraftClient
import net.minecraft.world.World
import net.minecraft.entity.ItemEntity

object EventBus {
    private val subscribers = mutableMapOf<KClass<*>, MutableList<(Any) -> Unit>>()

    fun publish(event: Any) {
        subscribers[event::class]?.forEach { it -> it(event) }
    }

    fun <T : Any> subscribe(eventType: KClass<T>, callback: (T) -> Unit) {
        val callbacks = subscribers.getOrPut(eventType) { mutableListOf() }
        callbacks.add(callback as (Any) -> Unit)
    }

    fun init() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { mc, world ->
            publish(WorldChangedEvent(mc, world))
        }

        ClientReceiveMessageEvents.GAME.register { message, _ ->
            publish(ChatEvent(message))
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            var event = ChatCancellableEvent(message, false)
            publish(event)
            !event.isCancelled
        }

        ScreenEvents.AFTER_INIT.register { client, screen, _, _ ->
            ScreenEvents.afterRender(screen).register { afterRenderScreen, drawContext, mouseX, mouseY, tickDelta ->
                publish(ScreenPostRenderEvent(drawContext, client.textRenderer, client, afterRenderScreen, mouseX, mouseY, tickDelta))
            }

            ScreenEvents.remove(screen).register {
                val guiName = when (screen) {
                    is ChatScreen -> "Chat"
                    is InventoryScreen -> "Inventory"
                    is HandledScreen<*> -> screen.getTitle().getString()
                    else -> screen.javaClass.getSimpleName()
                }
                publish(GuiClosedEvent(guiName))
            }

            ScreenMouseEvents.afterMouseClick(screen).register { scr, click, consumed ->
               publish(AfterMouseClickEvent(scr, click.x(), click.y(), click.buttonInfo().button))
               consumed
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            publish(ClientTickEvent(client))
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            publish(GameClosedEvent())
        }

        ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
            publish(GameStartedEvent())
        }

        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (entity is ItemEntity) {
                publish(ItemEntitySpawnedEvent(entity))
            }
        }
    }
}

