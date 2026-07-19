package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.overlays.FishingHookTimer;
import com.github.sleepypanda.feesh.features.rendering.HideOtherPlayersHooks;
import com.github.sleepypanda.feesh.features.rendering.RareMobHighlight;
import com.github.sleepypanda.feesh.features.rendering.HidePlayersNearBobber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Display.ItemDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void feesh$onShouldRender(T entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        switch (entity) {
            case ArmorStand armorStand -> {
                if (FishingHookTimer.shouldCancelArmorStandRendering(armorStand.getUUID())) {
                    cir.setReturnValue(false);
                }
            }
            case FishingHook fishingBobber -> {
                if (HideOtherPlayersHooks.shouldHideOtherPlayersHooks()) {
                    var player = Minecraft.getInstance().player;
                    if (player != null && fishingBobber.getPlayerOwner() != player) {
                        cir.setReturnValue(false);
                    }
                }
            }
            case Player playerEntity -> {
                if (HidePlayersNearBobber.shouldHidePlayer(playerEntity)) {
                    cir.setReturnValue(false);
                }
            }
            default -> {
            }
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void feesh$onExtractRenderState(T entity, S state, float tickProgress, CallbackInfo ci) {
        // Some rare creatures have Player entity type or Display entity type
        if (!(entity instanceof Mob) && !(entity instanceof Player) && !(entity instanceof ItemDisplay)) return;

        var id = entity.getId();
        if (!RareMobHighlight.highlightedEntities.containsKey(id)) return;

        var player = Minecraft.getInstance().player;
        if (player != null && player.hasLineOfSight(entity)) {
            state.outlineColor = RareMobHighlight.highlightedEntities.get(id);
        } else {
            state.outlineColor = 0; // Cleanup outline when entity became not visible
        }
    }
}