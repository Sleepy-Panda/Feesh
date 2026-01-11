package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.PlayerInteractEvent;
import com.github.sleepypanda.feesh.events.InteractActionType;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "interactItem", at = @At("HEAD"))
    private void feesh$onInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (hand != Hand.MAIN_HAND) return;
        EventBus.INSTANCE.publish(new PlayerInteractEvent(InteractActionType.USE_ITEM, true));
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void feesh$onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (hand != Hand.MAIN_HAND) return;
        EventBus.INSTANCE.publish(new PlayerInteractEvent(InteractActionType.USE_BLOCK, true));
    }
}
