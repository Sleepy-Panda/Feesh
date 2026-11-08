package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.utils.WaypointUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.gizmos.SimpleGizmoCollector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    //#if MC >= 26.2
    //$$ @Shadow
    //$$ @Final
    //$$ private SimpleGizmoCollector renderThreadGizmos;
    //#else
    @Shadow
    @Final
    private SimpleGizmoCollector collectedGizmos;
    //#endif

    // The method exists in 26.1, 26.2 and 26.3. Used as timing hook, there is nothing to deal with block breaking.
    // Can be replaced with submitFeatures probably after 26.1 is gone?
    @Inject(method = "submitBlockDestroyAnimation", at = @At("TAIL"))
    private void feesh$submitWaypoints(PoseStack poseStack, SubmitNodeCollector collector, LevelRenderState levelRenderState, CallbackInfo ci) {
        //#if MC >= 26.2
        //$$ SimpleGizmoCollector gizmos = this.renderThreadGizmos;
        //#else
        SimpleGizmoCollector gizmos = this.collectedGizmos;
        //#endif
        WaypointUtils.INSTANCE.render(collector, gizmos, levelRenderState.cameraRenderState);
    }
}
