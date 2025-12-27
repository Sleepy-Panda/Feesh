package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.ScreenRenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void feesh$onRenderScreen(
        DrawContext drawContext,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen screen = (Screen)(Object)this;
        EventBus.INSTANCE.publish(new ScreenRenderEvent(drawContext, client.textRenderer, client, screen, mouseX, mouseY, delta));
    }
}