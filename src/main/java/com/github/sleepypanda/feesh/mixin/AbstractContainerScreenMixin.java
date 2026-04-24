package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.AfterSlotRenderedEvent;
import com.github.sleepypanda.feesh.events.models.BeforeSlotRenderedEvent;
//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#else
import net.minecraft.client.gui.GuiGraphics;
//#endif
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    //#if MC >= 26.1
    //$$ @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;)V", at = @At("HEAD"), require = 0)
    //$$ private void feesh$onSlotBeforeItemDrawn(GuiGraphicsExtractor context, Slot slot, CallbackInfo ci) {
    //#else
    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", at = @At("HEAD"), require = 0)
    private void feesh$onSlotBeforeItemDrawn(GuiGraphics context, Slot slot, CallbackInfo ci) {
    //#endif
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new BeforeSlotRenderedEvent(context, slot, screen));
    }

    //#if MC >= 26.1
    //$$ @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;)V", at = @At("RETURN"), require = 0)
    //$$ private void feesh$onSlotAfterItemDrawn(GuiGraphicsExtractor context, Slot slot, CallbackInfo ci) {
    //#else
    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", at = @At("RETURN"), require = 0)
    private void feesh$onSlotAfterItemDrawn(GuiGraphics context, Slot slot, CallbackInfo ci) {
    //#endif
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new AfterSlotRenderedEvent(context, slot, screen));
    }
}
