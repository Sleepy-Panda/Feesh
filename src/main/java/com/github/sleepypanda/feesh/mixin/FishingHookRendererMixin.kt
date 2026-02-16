package com.github.sleepypanda.feesh.mixin

import com.github.sleepypanda.feesh.features.rendering.HideOtherPlayersHooks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.entity.FishingBobberEntityRenderer
import net.minecraft.entity.projectile.FishingBobberEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(FishingBobberEntityRenderer::class)
public abstract class FishingHookRendererMixin {
    @Suppress("UNUSED_PARAMETER")
    @Inject(method = ["shouldRender"], at = [At("HEAD")], cancellable = true)
    private fun feeshOnShouldRenderOtherBobbers(
        entity: FishingBobberEntity,
        _frustum: Frustum,
        _camX: Double,
        _camY: Double,
        _camZ: Double,
        cir: CallbackInfoReturnable<Boolean>
    ) {
        val currentPlayer = MinecraftClient.getInstance().player ?: return
        if (entity.owner != currentPlayer && HideOtherPlayersHooks.shouldHideOtherPlayersHooks()) {
            cir.setReturnValue(false)
        }
    }
}