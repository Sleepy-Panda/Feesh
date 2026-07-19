package com.github.sleepypanda.feesh.client.render.fluid;

//#if MC < 26.1
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jspecify.annotations.NonNull;

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
    public int getFluidColor(BlockAndTintGetter view, BlockPos pos, @NonNull FluidState state) {
        return tintColor;
    }

    @Override
    public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter view, BlockPos pos, @NonNull FluidState state) {
        return delegate.getFluidSprites(view, pos, state);
    }

    @Override
    public void reloadTextures(@NonNull TextureAtlas textureAtlas) {
        delegate.reloadTextures(textureAtlas);
    }

    @Override
    public void renderFluid(@NonNull BlockPos pos, @NonNull BlockAndTintGetter world, @NonNull VertexConsumer vertexConsumer, @NonNull BlockState blockState, @NonNull FluidState fluidState) {
        delegate.renderFluid(pos, world, vertexConsumer, blockState, fluidState);
    }
}
//#else
//$$ /** Stub: 26.x uses FluidModel swap in {@link com.github.sleepypanda.feesh.features.rendering.LavaRendering}. */
//$$ public final class TintedLavaRenderHandler {
//$$     private TintedLavaRenderHandler() {}
//$$ }
//#endif
