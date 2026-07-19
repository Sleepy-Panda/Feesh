package com.github.sleepypanda.feesh.mixin;

//#if MC >= 26.1
//$$ import com.github.sleepypanda.feesh.features.rendering.LavaRendering;
//$$ import com.mojang.blaze3d.platform.Transparency;
//$$ import net.minecraft.client.renderer.block.FluidModel;
//$$ import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$
//$$ /** Forces opaque chunk layer for Feesh lava-replacement fluid models. */
//$$ @Mixin(FluidModel.class)
//$$ public abstract class FluidModelMixin {
//$$     @Inject(method = "layer", at = @At("RETURN"), cancellable = true)
//$$     private void feesh$forceOpaqueLayer(CallbackInfoReturnable<ChunkSectionLayer> cir) {
//$$         if (LavaRendering.isLavaReplacementModel((FluidModel) (Object) this)) {
//$$             cir.setReturnValue(ChunkSectionLayer.byTransparency(Transparency.NONE));
//$$         }
//$$     }
//$$ }
//#else
public class FluidModelMixin {}
//#endif
