package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.ScreenAfterBackgroundRenderEvent;
import net.minecraft.client.Minecraft;
//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#else
import net.minecraft.client.gui.GuiGraphics;
//#endif
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    //#if MC >= 26.1
    //$$ @Inject(method = "extractBackground", at = @At("RETURN"), require = 0)
    //#else
    @Inject(method = "renderBackground", at = @At("RETURN"), require = 0)
    //#endif
    //#if MC >= 26.1
    //$$ private void feesh$onRenderBackgroundReturn(
    //$$     GuiGraphicsExtractor drawContext,
    //$$     int mouseX,
    //$$     int mouseY,
    //$$     float delta,
    //$$     CallbackInfo ci
    //$$ ) {
    //#else
    private void feesh$onRenderBackgroundReturn(
        GuiGraphics drawContext,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
    //#endif
        Screen screen = (Screen)(Object)this;
        // Only publish event for InventoryScreen
        if (screen instanceof InventoryScreen) {
            Minecraft client = Minecraft.getInstance();
            EventBus.INSTANCE.publish(new ScreenAfterBackgroundRenderEvent(drawContext, client.font, client, screen, mouseX, mouseY, delta));
        }
    }
}