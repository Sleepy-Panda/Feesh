package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.sounds.MuteJadeDragonSound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void feesh$onPlay_1_21_10(SoundInstance sound, CallbackInfo ci) {
        if (shouldCancel(sound)) {
            ci.cancel();
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At("HEAD"), cancellable = true, require = 0)
    private void feesh$onPlay_1_21_11(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (shouldCancel(sound)) {
            cir.cancel();
        }
    }

    private boolean shouldCancel(SoundInstance sound) {
        //#if MC >= 1.21.11
        //$$ return MuteJadeDragonSound.INSTANCE.shouldCancel(sound.getIdentifier());
        //#else
        return MuteJadeDragonSound.INSTANCE.shouldCancel(sound.getLocation());
        //#endif
    }
}
