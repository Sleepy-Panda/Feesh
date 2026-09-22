package com.github.sleepypanda.feesh.utils

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.input.InputWithModifiers
//#if MC >= 26.3
//$$ // GLFW was replaced by SDL in 26.3; use InputConstants instead.
//#else
import org.lwjgl.glfw.GLFW
//#endif

object InputUtils {
    fun unboundKey(): Int {
        //#if MC >= 26.3
        //$$ return InputConstants.UNKNOWN.getValue()
        //#else
        return GLFW.GLFW_KEY_UNKNOWN
        //#endif
    }

    fun pauseKey(): Int {
        //#if MC >= 26.3
        //$$ return InputConstants.KEY_PAUSE
        //#else
        return GLFW.GLFW_KEY_PAUSE
        //#endif
    }

    fun keyType(): InputConstants.Type {
        //#if MC >= 26.3
        //$$ return InputConstants.Type.KEYBOARD
        //#else
        return InputConstants.Type.KEYSYM
        //#endif
    }

    fun hasControlDown(modifiers: Int): Boolean {
        return (modifiers and InputConstants.MOD_CONTROL) != 0
    }

    fun hasControlDown(input: InputWithModifiers?): Boolean = input?.let { hasControlDown(it.modifiers()) } ?: false

    fun isLeftMouseButton(button: Int): Boolean {
        //#if MC >= 26.3
        //$$ return button == InputConstants.MOUSE_BUTTON_LEFT
        //#else
        return button == 0
        //#endif
    }

    fun isEscapeKey(keyCode: Int): Boolean {
        //#if MC >= 26.3
        //$$ return keyCode == InputConstants.KEY_ESCAPE
        //#else
        return keyCode == GLFW.GLFW_KEY_ESCAPE
        //#endif
    }

    fun isPlusKey(keyCode: Int): Boolean {
        //#if MC >= 26.3
        //$$ return keyCode == InputConstants.KEY_EQUALS || keyCode == InputConstants.KEY_ADD
        //#else
        return keyCode == GLFW.GLFW_KEY_EQUAL || keyCode == GLFW.GLFW_KEY_KP_ADD
        //#endif
    }

    fun isMinusKey(keyCode: Int): Boolean {
        //#if MC >= 26.3
        //$$ return keyCode == InputConstants.KEY_MINUS
        //#else
        return keyCode == GLFW.GLFW_KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT
        //#endif
    }

    fun isZeroKey(keyCode: Int): Boolean {
        //#if MC >= 26.3
        //$$ return keyCode == InputConstants.KEY_0
        //#else
        return keyCode == GLFW.GLFW_KEY_0
        //#endif
    }
}
