package com.github.sleepypanda.feesh.events

import com.github.sleepypanda.feesh.events.models.AfterMouseClickEvent
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.GameStartedEvent
import com.github.sleepypanda.feesh.events.models.GuiClosedEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.ItemEntityLoadedEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.events.models.ScreenBeforeInitEvent
import kotlin.reflect.KClass
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
//#if MC >= 26.1
//$$ import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents as ClientWorldEvents
//#else
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
//#endif
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.network.chat.Component
import net.minecraft.client.Minecraft
import net.minecraft.world.level.Level
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand

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
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            publish(ChatEvent(message))
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, isOverlay ->
            var event = ChatCancellableEvent(message, false, isOverlay)
            publish(event)
            !event.isCancelled
        }

        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            publish(ScreenBeforeInitEvent(screen))
        }

        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            ScreenEvents.remove(screen).register {
                val guiName = when (screen) {
                    is ChatScreen -> "Chat"
                    is InventoryScreen -> "Inventory"
                    is AbstractContainerScreen<*> -> screen.title.string
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

        //#if MC >= 26.1
        //$$ ClientWorldEvents.AFTER_CLIENT_LEVEL_CHANGE.register { mc, world ->
        //#else
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { mc, world ->
        //#endif
            publish(WorldChangedEvent(mc, world))
        }

        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            when (entity) {
                is ItemEntity -> publish(ItemEntityLoadedEvent(entity))
                is ArmorStand -> if (entity.isAlive) publish(ArmorStandLoadedEvent(entity))
                else -> { }
            }
        }

        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            if (entity is ArmorStand) {
                publish(ArmorStandDespawnedEvent(entity))
            }
        }
    }
}

