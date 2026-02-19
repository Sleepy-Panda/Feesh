package com.github.sleepypanda.feesh.client.render.fluid;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

/**
 * Wraps a FluidRenderHandler (e.g. water) and overrides only getFluidColor.
 * Used to show tinted water instead of lava: same sprites/rendering as the
 * delegate, but with a custom tint. Delegates so sprites are always loaded
 * (Sodium/crash fix: we must not create a new handler whose reloadTextures()
 * is never called).
 */
public final class TintedLavaRenderHandler implements FluidRenderHandler {
    private final FluidRenderHandler delegate;
    private final int tintColor;

    public TintedLavaRenderHandler(FluidRenderHandler delegate, int tintColor) {
        this.delegate = delegate;
        this.tintColor = tintColor;
    }

    @Override
    public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state) {
        return tintColor;
    }

    @Override
    public Sprite[] getFluidSprites(BlockRenderView view, BlockPos pos, FluidState state) {
        return delegate.getFluidSprites(view, pos, state);
    }

    @Override
    public void reloadTextures(SpriteAtlasTexture textureAtlas) {
        delegate.reloadTextures(textureAtlas);
    }

    @Override
    public void renderFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        delegate.renderFluid(pos, world, vertexConsumer, blockState, fluidState);
    }
}
