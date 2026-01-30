package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.GuiClosedEvent;
import com.github.sleepypanda.feesh.events.GuiOpenedEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    private Screen currentScreen;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void feesh$onScreenChanged(Screen screen, CallbackInfo ci) {
        if (screen != null) {
            EventBus.INSTANCE.publish(new GuiOpenedEvent(screen));
        } else if (currentScreen != null) {
            String guiName = currentScreen instanceof HandledScreen<?> handled
                ? handled.getTitle().getString()
                : currentScreen.getClass().getSimpleName();
            EventBus.INSTANCE.publish(new GuiClosedEvent(guiName));
        }
    }
}
