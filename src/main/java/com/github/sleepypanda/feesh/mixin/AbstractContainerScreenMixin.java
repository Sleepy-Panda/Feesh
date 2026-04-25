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
    //$$ @Inject(method = "extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/item/ItemStack;III)V"), require = 1)
    //$$ private void feesh$onSlotBeforeItemDrawn(GuiGraphicsExtractor context, Slot slot, int x, int y, CallbackInfo ci) {
    //#else
    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", at = @At("HEAD"), require = 0)
    private void feesh$onSlotBeforeItemDrawn(GuiGraphics context, Slot slot, CallbackInfo ci) {
    //#endif
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new BeforeSlotRenderedEvent(context, slot, screen));
    }

    //#if MC == 1.21.11
    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V", at = @At("HEAD"), require = 0)
    private void feesh$onSlotBeforeItemDrawn_1_21_11(GuiGraphics context, Slot slot, int x, int y, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new BeforeSlotRenderedEvent(context, slot, screen));
    }
    //#endif

    //#if MC >= 26.1
    //$$ @Inject(method = "extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"), require = 1)
    //$$ private void feesh$onSlotAfterItemDrawn(GuiGraphicsExtractor context, Slot slot, int x, int y, CallbackInfo ci) {
    //#else
    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", at = @At("RETURN"), require = 0)
    private void feesh$onSlotAfterItemDrawn(GuiGraphics context, Slot slot, CallbackInfo ci) {
    //#endif
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new AfterSlotRenderedEvent(context, slot, screen));
    }

    //#if MC == 1.21.11
    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V", at = @At("RETURN"), require = 0)
    private void feesh$onSlotAfterItemDrawn_1_21_11(GuiGraphics context, Slot slot, int x, int y, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new AfterSlotRenderedEvent(context, slot, screen));
    }
    //#endif
}
