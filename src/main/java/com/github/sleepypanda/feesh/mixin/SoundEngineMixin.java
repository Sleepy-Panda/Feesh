package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.features.sounds.MuteJadeDragonSound;
import com.github.sleepypanda.feesh.features.sounds.MuteReindrakeGifts;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At("HEAD"), cancellable = true)
    private void feesh$onPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (shouldCancel(sound)) {
            cir.cancel();
        }
    }

    private boolean shouldCancel(SoundInstance sound) {
        return MuteJadeDragonSound.INSTANCE.shouldCancel(sound.getIdentifier()) || MuteReindrakeGifts.INSTANCE.shouldCancel(sound.getIdentifier());
    }
}
