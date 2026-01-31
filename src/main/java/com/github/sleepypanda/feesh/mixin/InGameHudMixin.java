
package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.GameRenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    // Lets to draw custom content UNDER TabList and the rest of the HUD.
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderPlayerList(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = At.Shift.BEFORE))
    private void feesh$onRenderInGameHudBeforePlayerList(
        DrawContext drawContext,
        RenderTickCounter renderTickCounter,
        CallbackInfo ci
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        EventBus.INSTANCE.publish(new GameRenderEvent(drawContext, client.textRenderer, client, renderTickCounter));
    }
}