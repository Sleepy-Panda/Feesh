package com.github.sleepypanda.feesh.utils

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.input.InputWithModifiers

object InputUtils {
    fun unboundKey(): Int = InputConstants.UNKNOWN.getValue()

    fun pauseKey(): Int = InputConstants.KEY_PAUSE

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

    fun isLeftMouseButton(button: Int): Boolean = button == InputConstants.MOUSE_BUTTON_LEFT

    fun isEscapeKey(keyCode: Int): Boolean = keyCode == InputConstants.KEY_ESCAPE

    fun isPlusKey(keyCode: Int): Boolean {
        return keyCode == InputConstants.KEY_EQUALS || keyCode == InputConstants.KEY_ADD
    }

    fun isMinusKey(keyCode: Int): Boolean = keyCode == InputConstants.KEY_MINUS

    fun isZeroKey(keyCode: Int): Boolean = keyCode == InputConstants.KEY_0
}
