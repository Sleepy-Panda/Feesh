
package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.GameRenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#else
import net.minecraft.client.gui.GuiGraphics;
//#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    // Lets to draw custom content UNDER TabList and the rest of the HUD.
    //#if MC >= 26.1
    //$$ @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.BEFORE), require = 0)
    //$$ private void feesh$onRenderInGameHudBeforePlayerList(
    //$$     GuiGraphicsExtractor drawContext,
    //$$     DeltaTracker renderTickCounter,
    //$$     CallbackInfo ci
    //$$ ) {
    //#else
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTabList(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.BEFORE), require = 0)
    private void feesh$onRenderInGameHudBeforePlayerList(
        GuiGraphics drawContext,
        DeltaTracker renderTickCounter,
        CallbackInfo ci
    ) {
    //#endif
        Minecraft client = Minecraft.getInstance();
        EventBus.INSTANCE.publish(new GameRenderEvent(drawContext, client.font, client, renderTickCounter));
    }
}