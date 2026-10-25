
package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.GameRenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
//#if MC >= 26.2
//$$ import net.minecraft.client.gui.Hud;
//#else
import net.minecraft.client.gui.Gui;
//#endif
import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 26.2
//$$ @Mixin(Hud.class)
//#else
@Mixin(Gui.class)
//#endif
public class GuiMixin {
    // Lets to draw custom content UNDER TabList and the rest of the HUD.
    //#if MC >= 26.2
    //$$ @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.BEFORE))
    //#else
    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.BEFORE))
    //#endif
    private void feesh$onRenderInGameHudBeforePlayerList(
        GuiGraphicsExtractor drawContext,
        DeltaTracker renderTickCounter,
        CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        EventBus.INSTANCE.publish(new GameRenderEvent(drawContext, client.font, client, renderTickCounter));
    }
}
