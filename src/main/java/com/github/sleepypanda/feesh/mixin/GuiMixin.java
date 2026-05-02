
package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.AfterHotbarSlotRenderedEvent;
import com.github.sleepypanda.feesh.events.models.GameRenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
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
    //$$ @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.BEFORE))
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

    /**
     * Vanilla {@code Gui.renderItemHotbar} calls {@code renderHotbarItem(..., hotbarSlotIndex)} with the loop index 0..8.
     * Mojang still names that parameter {@code seed} in mappings; it is the hotbar column, not a random seed here.
     */
    //#if MC >= 26.1
    //$$ @Inject(method = "renderHotbarItem", at = @At("RETURN"), require = 0)
    //$$ private void feesh$afterHotbarSlotItem(
    //$$     GuiGraphicsExtractor context,
    //$$     int x,
    //$$     int y,
    //$$     float tickDelta,
    //$$     LocalPlayer player,
    //$$     ItemStack stack,
    //$$     int hotbarSlotIndex,
    //$$     CallbackInfo ci
    //$$ ) {
    //$$     if (hotbarSlotIndex < 0 || hotbarSlotIndex > 8) return;
    //$$     Minecraft mc = Minecraft.getInstance();
    //$$     EventBus.INSTANCE.publish(
    //$$         new AfterHotbarSlotRenderedEvent(context, mc.font, hotbarSlotIndex, x, y, stack));
    //$$ }
    //#else
    @Inject(method = "renderHotbarItem", at = @At("RETURN"), require = 0)
    private void feesh$afterHotbarSlotItem(
        GuiGraphics context,
        int x,
        int y,
        float tickDelta,
        LocalPlayer player,
        ItemStack stack,
        int hotbarSlotIndex,
        CallbackInfo ci
    ) {
        if (hotbarSlotIndex < 0 || hotbarSlotIndex > 8) return;
        Minecraft mc = Minecraft.getInstance();
        EventBus.INSTANCE.publish(new AfterHotbarSlotRenderedEvent(context, mc.font, hotbarSlotIndex, x, y, stack));
    }
    //#endif
}