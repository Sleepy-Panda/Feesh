package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.ScreenAfterBackgroundRenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void feesh$onRenderBackgroundReturn(
        DrawContext drawContext,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        Screen screen = (Screen)(Object)this;
        // Only publish event for InventoryScreen
        if (screen instanceof InventoryScreen) {
            MinecraftClient client = MinecraftClient.getInstance();
            EventBus.INSTANCE.publish(new ScreenAfterBackgroundRenderEvent(drawContext, client.textRenderer, client, screen, mouseX, mouseY, delta));
        }
    }
}