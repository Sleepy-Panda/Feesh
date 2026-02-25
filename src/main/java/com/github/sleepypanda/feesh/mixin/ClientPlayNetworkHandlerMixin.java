package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.EventBus;
import com.github.sleepypanda.feesh.events.models.ParticleSpawnedEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onParticle", at = @At("RETURN"))
	private void feesh$onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
        EventBus.INSTANCE.publish(new ParticleSpawnedEvent(packet.getParameters().getType(), packet.getCount(), packet.getSpeed(), packet.getX(), packet.getY(), packet.getZ()));
	}
}
