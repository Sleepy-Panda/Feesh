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

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void feesh$onSlotBeforeItemDrawn(DrawContext context, Slot slot, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new BeforeSlotRenderedEvent(context, slot, screen));
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void feesh$onSlotAfterItemDrawn(DrawContext context, Slot slot, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        EventBus.INSTANCE.publish(new AfterSlotRenderedEvent(context, slot, screen));
    }
}
