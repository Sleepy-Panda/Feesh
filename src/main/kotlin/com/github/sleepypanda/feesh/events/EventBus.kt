package com.github.sleepypanda.feesh.events

import kotlin.reflect.KClass
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.text.Text
import net.minecraft.client.MinecraftClient
import net.minecraft.world.World

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

        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            ScreenEvents.afterRender(screen).register { afterRenderScreen, drawContext, mouseX, mouseY, tickDelta ->
                publish(ScreenPostRenderEvent(drawContext, client.textRenderer, client, afterRenderScreen, mouseX, mouseY, tickDelta))
            }
        }
    }
}

