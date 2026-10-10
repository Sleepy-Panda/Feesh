package com.github.sleepypanda.feesh.mixin;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSoundInstance.class)
public interface AbstractSoundInstanceAccessor {
    @Accessor("volume")
    float feesh$getRawVolume();

    @Accessor("pitch")
    float feesh$getRawPitch();
}
