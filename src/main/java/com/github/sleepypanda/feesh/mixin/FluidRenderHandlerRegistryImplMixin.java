package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.client.render.fluid.TintedLavaRenderHandler;
import com.github.sleepypanda.feesh.features.rendering.LavaRendering;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;

// Credits to https://github.com/hannibal002/SkyHanni/blob/d5d1ba90c96f945237dfad224b9ddf3397748098/src/main/java/at/hannibal2/skyhanni/mixins/transformers/MixinFluidRenderHandlerRegistry.java#L28
@Mixin(value = FluidRenderHandlerRegistryImpl.class, remap = false)
public class FluidRenderHandlerRegistryImplMixin {
    @Shadow
    @Final
    private Map<Fluid, FluidRenderHandler> handlers;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void feesh$replaceLavaWithWater(Fluid fluid, CallbackInfoReturnable<FluidRenderHandler> cir) {
        if (fluid != Fluids.LAVA && fluid != Fluids.FLOWING_LAVA) return;

        if (LavaRendering.shouldReplaceLavaWithWater()) {
            if (fluid == Fluids.LAVA) cir.setReturnValue(handlers.get(Fluids.WATER));
            else if (fluid == Fluids.FLOWING_LAVA) cir.setReturnValue(handlers.get(Fluids.FLOWING_WATER));
            return;
        }

        if (LavaRendering.shouldTintLava()) {
            int lavaTint = LavaRendering.getLavaTintColor();
            if (lavaTint != 0) {
                FluidRenderHandler waterHandler = fluid == Fluids.LAVA ? handlers.get(Fluids.WATER) : handlers.get(Fluids.FLOWING_WATER);
                if (waterHandler != null) {
                    cir.setReturnValue(new TintedLavaRenderHandler(waterHandler, lavaTint));
                }
            }
            return;
        }
    }
}
