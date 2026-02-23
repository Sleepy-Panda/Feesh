package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.GuiOpenedEvent;
import com.github.sleepypanda.feesh.events.models.WorldUnloadEvent;
import com.github.sleepypanda.feesh.features.rendering.RareMobHighlight;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    private ClientWorld world;

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void feesh$forceOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (RareMobHighlight.highlightedEntities.containsKey(entity.getId())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "joinWorld", at = @At("HEAD"))
    private void feesh$onWorldUnload(ClientWorld newWorld, CallbackInfo ci) {
        if (this.world != null) {
            EventBus.INSTANCE.publish(new WorldUnloadEvent());
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void feesh$onScreenOpened(Screen screen, CallbackInfo ci) {
        if (screen != null) {
            EventBus.INSTANCE.publish(new GuiOpenedEvent(screen));
        }
    }
}
