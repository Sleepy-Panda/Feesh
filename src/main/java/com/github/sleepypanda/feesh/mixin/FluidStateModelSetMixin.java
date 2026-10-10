package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.rendering.LavaRendering;
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
 * Swaps lava for a water-textured fluid model while lava replacement is on.
 */
@Mixin(FluidStateModelSet.class)
public abstract class FluidStateModelSetMixin {
    @Inject(method = "bake", at = @At("RETURN"))
    private static void feesh$bakeLavaReplacement(MaterialBaker materials, CallbackInfoReturnable<Map<Fluid, FluidModel>> cir) {
        LavaRendering.bakeLavaReplacementModel(materials);
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void feesh$replaceLavaModel(FluidState state, CallbackInfoReturnable<FluidModel> cir) {
        Fluid fluid = state.getType();
        if (fluid != Fluids.LAVA && fluid != Fluids.FLOWING_LAVA) return;
        if (!LavaRendering.isActive()) return;

        FluidModel replacement = LavaRendering.getLavaReplacementModel();
        if (replacement != null) {
            cir.setReturnValue(replacement);
        }
    }
}
