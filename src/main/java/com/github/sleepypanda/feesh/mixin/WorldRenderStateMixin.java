package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.rendering.RareMobHighlight;
import net.minecraft.client.render.state.WorldRenderState; // TARGET THIS
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderState.class)
public abstract class WorldRenderStateMixin {

    @Shadow
    public boolean hasOutline;

    @Inject(method = "clear", at = @At("TAIL"))
    private void feesh$forceHasOutline(CallbackInfo ci) {
        if (!RareMobHighlight.highlightedEntities.isEmpty()) {
            this.hasOutline = true;
        }
    }
}
