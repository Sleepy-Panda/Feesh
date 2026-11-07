package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.rendering.ReplaceAndTintLava;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;

/**
 * Swap lava and flowing lava for a water fluid models while lava replacement is on.
 */
@Mixin(FluidStateModelSet.class)
public abstract class FluidStateModelSetMixin {
    @Inject(method = "bake", at = @At("RETURN"))
    private static void feesh$bakeLavaReplacement(MaterialBaker materials, CallbackInfoReturnable<Map<Fluid, FluidModel>> cir) {
        ReplaceAndTintLava.bakeLavaReplacementModel(materials);
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void feesh$replaceLavaModel(FluidState state, CallbackInfoReturnable<FluidModel> cir) {
        Fluid fluid = state.getType();
        if (fluid != Fluids.LAVA && fluid != Fluids.FLOWING_LAVA) return;
        if (!ReplaceAndTintLava.isLavaReplacementActive()) return;

        FluidModel replacement = ReplaceAndTintLava.getLavaReplacementModel();
        if (replacement != null) {
            cir.setReturnValue(replacement);
        }
    }
}
