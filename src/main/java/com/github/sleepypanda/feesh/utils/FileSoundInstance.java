package com.github.sleepypanda.feesh.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

/**
 * Plays .ogg file through Minecraft's sound engine. No resource pack needed.
 * SoundEngine only opens a stream when the resolved sound has "stream": true.
 * DUMMY_FILE_ID is that entry in the mod sounds.json.
 */
public class FileSoundInstance extends AbstractSoundInstance {
    // This is a dummy stream for custom sounds placed in a folder.
    // Must have stream: true in the sounds.json to work.
    public static final Identifier DUMMY_FILE_ID = Identifier.fromNamespaceAndPath(SoundUtils.SOUNDS_IDENTIFIER_PREFIX, "feesh_dummy_stream_for_custom_sounds");

    private final File file;

    public FileSoundInstance(File file) {
        super(DUMMY_FILE_ID, SoundSource.UI, SoundInstance.createUnseededRandom());
        this.file = file;
        this.volume = 0.8f;
        this.pitch = 1f;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
    }

    // Fabric adds getAudioStream to SoundInstance at runtime (mixin).
    // This method replaces Fabric's method and is what the sound engine calls.
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary library, Identifier id, boolean repeatInstantly) {
        CompletableFuture<AudioStream> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                future.complete(new JOrbisAudioStream(new FileInputStream(file)));
            } catch (IOException e) {
                future.completeExceptionally(new RuntimeException("Failed to get stream for sound file: " + file.getAbsolutePath(), e));
            }
        });
        return future;
    }
}
