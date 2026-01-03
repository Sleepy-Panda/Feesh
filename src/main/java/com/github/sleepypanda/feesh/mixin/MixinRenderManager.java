package com.github.sleepypanda.feesh.mixin;
//
//import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
//import net.minecraft.entity.decoration.ArmorStandEntity;
//import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
//import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//import net.minecraft.client.render.state.CameraRenderState;
//import net.minecraft.client.util.math.MatrixStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(ArmorStandEntityRenderer.class)
//public class ArmorStandEntityRendererMixin {
//    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
//    private void feesh$onRenderArmorStand(
//        ArmorStandEntityRenderState armorStandEntityRenderState,
//        MatrixStack matrixStack,
//        OrderedRenderCommandQueue orderedRenderCommandQueue,
//        CameraRenderState cameraRenderState,
//        CallbackInfo ci
//    ) {
//        ArmorStandEntity armorStand = armorStandEntityRenderState.id
//        if (com.github.sleepypanda.feesh.features.overlays.FishingHookTimer.shouldCancelArmorStandRendering(armorStand.getUuid())) {
//            ci.cancel();
//        }
//    }
//}
//
//
//import net.minecraft.entity.Entity;
//import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
//import net.minecraft.client.render.Frustum;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(EntityRenderDispatcher.class)
//public class MixinRenderManager {
//
//    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
//    private void shouldRender(Entity entity, Frustum camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
//        if (com.github.sleepypanda.feesh.features.overlays.FishingHookTimer.shouldCancelArmorStandRendering(armorStand.getUuid())) {
//            cir.setReturnValue(false);
//        }
//    }
//}