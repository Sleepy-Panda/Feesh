package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.sounds.MuteJadeDragonSound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void feesh$onPlay_1_21_10(SoundInstance sound, CallbackInfo ci) {
        if (shouldCancel(sound)) {
            ci.cancel();
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;", at = @At("HEAD"), cancellable = true, require = 0)
    private void feesh$onPlay_1_21_11(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (shouldCancel(sound)) {
            cir.cancel();
        }
    }

    private boolean shouldCancel(SoundInstance sound) {
        return MuteJadeDragonSound.INSTANCE.shouldCancel(sound.getId());
    }
}
