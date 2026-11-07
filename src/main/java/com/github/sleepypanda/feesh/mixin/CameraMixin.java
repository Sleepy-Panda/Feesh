package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.rendering.ReplaceAndTintLava;
import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Camera in lava reported as camera in water while lava replacement is on.
 * This is done for the orange lava fog to go away.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
    private void feesh$lavaCameraAsWater(CallbackInfoReturnable<FogType> cir) {
        if (cir.getReturnValue() != FogType.LAVA) return;
        if (!ReplaceAndTintLava.isLavaReplacementActive()) return;
        cir.setReturnValue(FogType.WATER);
    }
}
