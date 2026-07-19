package com.github.sleepypanda.feesh.utils

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.input.InputQuirks
import net.minecraft.client.input.InputWithModifiers

object InputUtils {
    fun hasControlDown(modifiers: Int): Boolean {
        return (modifiers and InputConstants.MOD_CONTROL) != 0
    }

    fun hasControlDown(input: InputWithModifiers?): Boolean = input?.let { hasControlDown(it.modifiers()) } ?: false
}
