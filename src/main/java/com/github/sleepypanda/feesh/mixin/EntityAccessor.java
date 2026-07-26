package com.github.sleepypanda.feesh.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

// https://docs.fabricmc.net/develop/mixins/accessors#field-accessors

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("DATA_CUSTOM_NAME")
    static EntityDataAccessor<Optional<Component>> feesh$getDataCustomName() {
        throw new AssertionError("Untransformed @Accessor for Entity.DATA_CUSTOM_NAME"); // stub is never invoked, but body is required for static member access
    }
}
