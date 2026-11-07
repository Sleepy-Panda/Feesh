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
public abstract class WaypointLabelMixin {
    //#if MC >= 26.2
    //$$ @Shadow
    //$$ @Final
    //$$ private SimpleGizmoCollector renderThreadGizmos;
    //#else
    @Shadow
    @Final
    private SimpleGizmoCollector collectedGizmos;
    //#endif

    // Before gizmos are finalized, including the early return when nothing is breaking.
    @Inject(method = "submitBlockDestroyAnimation", at = @At("TAIL"))
    private void feesh$submitWaypoints(PoseStack poseStack, SubmitNodeCollector collector, LevelRenderState levelRenderState, CallbackInfo ci) {
        //#if MC >= 26.2
        //$$ SimpleGizmoCollector gizmos = this.renderThreadGizmos;
        //#else
        SimpleGizmoCollector gizmos = this.collectedGizmos;
        //#endif
        WaypointUtils.INSTANCE.submit(collector, gizmos, levelRenderState.cameraRenderState);
    }
}
