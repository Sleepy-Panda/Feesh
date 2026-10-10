package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.SoundPlayedEvent;
import com.github.sleepypanda.feesh.features.sounds.MuteJadeDragonSound;
import com.github.sleepypanda.feesh.features.sounds.MuteReindrakeGifts;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
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
        try {
            var soundId = sound.getIdentifier();
            // getVolume()/getPitch() NPE at HEAD: AbstractSoundInstance.sound is still null until play() resolves it.
            // AbstractSoundInstanceAccessor is used to get the raw volume and pitch.
            if (soundId != null && sound instanceof AbstractSoundInstance) {
                AbstractSoundInstanceAccessor accessor = (AbstractSoundInstanceAccessor) sound;
                EventBus.INSTANCE.publish(new SoundPlayedEvent(
                        soundId,
                        accessor.feesh$getRawVolume(),
                        accessor.feesh$getRawPitch(),
                        sound.getX(),
                        sound.getY(),
                        sound.getZ(),
                        sound.isRelative()
                ));
            }
            
            if (shouldCancel(sound)) {
                cir.cancel();
            }    
        } catch (Exception e) { }
    }

    private boolean shouldCancel(SoundInstance sound) {
        float volume = sound instanceof AbstractSoundInstance
                ? ((AbstractSoundInstanceAccessor) sound).feesh$getRawVolume()
                : 1.0f;
        return MuteJadeDragonSound.shouldCancel(sound.getIdentifier())
                || MuteReindrakeGifts.shouldCancel(sound.getIdentifier(), volume);
    }
}
