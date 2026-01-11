package com.github.sleepypanda.feesh.events

import net.minecraft.item.ItemStack

enum class InteractActionType {
    USE_ITEM,
    USE_BLOCK
}

data class PlayerInteractEvent(
    val actionType: InteractActionType,
    val isMainHand: Boolean
)
