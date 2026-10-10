package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.rendering.LavaRendering;
import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Eyes in lava report as water while lava replacement is on.
 * Vanilla fog and shader packs both read this, so the orange lava fog goes away.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
    private void feesh$lavaCameraAsWater(CallbackInfoReturnable<FogType> cir) {
        if (cir.getReturnValue() != FogType.LAVA) return;
        if (!LavaRendering.isActive()) return;
        cir.setReturnValue(FogType.WATER);
    }
}
