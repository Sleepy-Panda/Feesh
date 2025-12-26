package com.github.sleepypanda.feesh.events

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.RegisterUtils
import kotlin.reflect.KClass
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
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

        // TODO - All chat listener, move to SeaCreaturesApi
        // Register chat listeners for all rare sea creatures
        SeaCreatures.allSeaCreatures
            .forEach { sc ->
                RegisterUtils.chat(Regex(sc.pattern)) { message, _ -> publish(SeaCreatureSpawnedEvent(sc.name, message.string)) }
            }
    }
}

