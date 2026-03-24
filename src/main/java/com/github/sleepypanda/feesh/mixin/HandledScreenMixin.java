package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.AfterSlotRenderedEvent;
import com.github.sleepypanda.feesh.events.models.BeforeSlotRenderedEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for HandledScreen (Yarn name for AbstractContainerScreen).
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", at = @At("HEAD"), require = 0)
    private void feesh$onSlotBeforeItemDrawn_1_21_10(DrawContext context, Slot slot, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new BeforeSlotRenderedEvent(context, slot, screen));
    }

    @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V", at = @At("HEAD"), require = 0)
    private void feesh$onSlotBeforeItemDrawn_1_21_11(DrawContext context, Slot slot, int x, int y, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new BeforeSlotRenderedEvent(context, slot, screen));
    }

    @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", at = @At("RETURN"), require = 0)
    private void feesh$onSlotAfterItemDrawn_1_21_10(DrawContext context, Slot slot, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new AfterSlotRenderedEvent(context, slot, screen));
    }

    @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V", at = @At("RETURN"), require = 0)
    private void feesh$onSlotAfterItemDrawn_1_21_11(DrawContext context, Slot slot, int x, int y, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new AfterSlotRenderedEvent(context, slot, screen));
    }
}
