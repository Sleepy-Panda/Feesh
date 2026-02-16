package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.overlays.FishingHookTimer;
import com.github.sleepypanda.feesh.features.rendering.HideOtherPlayersHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void feesh$onShouldRender(T entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        switch (entity) {
            case ArmorStandEntity armorStand:
                if (FishingHookTimer.shouldCancelArmorStandRendering(armorStand.getUuid())) {
                    cir.setReturnValue(false);
                }
                break;
            case FishingBobberEntity fishingBobber:
                if (HideOtherPlayersHooks.shouldHideOtherPlayersHooks()) {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null && fishingBobber.getOwner() != player) {
                        cir.setReturnValue(false);
                    }
                }
                break;
            default:
                break;
        }
    }
}
