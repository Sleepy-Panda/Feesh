package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.InteractActionType;
import com.github.sleepypanda.feesh.events.models.PlayerInteractEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "interactItem", at = @At("HEAD"))
    private void feesh$onInteractItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (hand != InteractionHand.MAIN_HAND) return;
        EventBus.INSTANCE.publish(new PlayerInteractEvent(InteractActionType.USE_ITEM, true));
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void feesh$onInteractBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (hand != InteractionHand.MAIN_HAND) return;
        EventBus.INSTANCE.publish(new PlayerInteractEvent(InteractActionType.USE_BLOCK, true));
    }
}
