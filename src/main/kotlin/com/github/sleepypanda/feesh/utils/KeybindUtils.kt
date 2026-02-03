package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import org.lwjgl.glfw.GLFW

object KeybindUtils {
    val FEESH_CATEGORY = KeyBinding.Category(Identifier.of("feesh", "keybinds")) // Keys are localized in resources/assets/feesh/lang/en_us.json
    
    private val keybinds = mutableMapOf<KeyBinding, () -> Unit>()

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    fun registerKeybind(id: String, keyCode: Int = GLFW.GLFW_KEY_UNKNOWN, callback: () -> Unit): KeyBinding {
        val keyBinding = KeyBinding(
            id,
            InputUtil.Type.KEYSYM,
            keyCode,
            FEESH_CATEGORY
        )
        KeyBindingHelper.registerKeyBinding(keyBinding)
        keybinds[keyBinding] = callback
        return keyBinding
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        keybinds.forEach { (keyBinding, callback) ->
            if (keyBinding.wasPressed()) {
                callback()
            }
        }
    }
}
