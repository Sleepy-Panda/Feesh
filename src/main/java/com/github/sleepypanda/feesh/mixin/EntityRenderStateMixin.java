package com.github.sleepypanda.feesh.mixin;

import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin {
    @Shadow public int outlineColor;
}