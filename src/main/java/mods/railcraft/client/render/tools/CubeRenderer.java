/*******************************************************************************
 * Copyright (c) CovertJaguar, 2011-2016
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/

package mods.railcraft.client.render.tools;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;

/**
 * Created by CovertJaguar on 5/31/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class CubeRenderer {
    public static class RenderInfo {

        public static final float LIGHT_SOURCE_FULL = 1.0F;

        public final Side[] sides = new Side[6];
        public AxisAlignedBB boundingBox;
        public boolean glow;
        public float lightSource = LIGHT_SOURCE_FULL;

        public RenderInfo() {
            for (int i = 0; i < sides.length; i++) {
                sides[i] = new Side();
            }
        }

        public final void resetSidesAndLight() {
            Arrays.stream(sides).forEach(side -> {
                side.render = false;
                side.texture = null;
            });
            lightSource = LIGHT_SOURCE_FULL;
        }

        public final RenderInfo glow() {
            this.glow = true;
            return this;
        }

        public final RenderInfo lightSource(Entity entity, float partialTicks) {
            lightSource = entity.getBrightness(partialTicks);
            return this;
        }

        public final RenderInfo lightSource(World world, BlockPos pos) {
            lightSource = world.getLightBrightness(pos);
            return this;
        }

        public final RenderInfo setBoundingBox(AxisAlignedBB boundingBox) {
            this.boundingBox = boundingBox;
            return this;
        }

        public final RenderInfo setRenderSide(EnumFacing side, boolean render) {
            sides[side.ordinal()].render = render;
            return this;
        }

        public final RenderInfo setRenderAllSides() {
            Arrays.stream(sides).forEach(side -> side.render = true);
            return this;
        }

        public final RenderInfo setTextures(TextureAtlasSprite[] textures) {
            Arrays.stream(EnumFacing.VALUES).forEach(s -> {
                sides[s.ordinal()].texture = textures[s.ordinal()];
                sides[s.ordinal()].render = true;
            });
            return this;
        }

        public final RenderInfo setTexture(EnumFacing side, TextureAtlasSprite texture) {
            sides[side.ordinal()].texture = texture;
            sides[side.ordinal()].render = true;
            return this;
        }

        public final RenderInfo setTextureToAllSides(TextureAtlasSprite texture) {
            Arrays.stream(sides).forEach(side -> side.texture = texture);
            setRenderAllSides();
            return this;
        }

        public TextureAtlasSprite getTexture(EnumFacing side) {
            return sides[side.ordinal()].texture;
        }

        public static class Side {
            public TextureAtlasSprite texture;
            public boolean render;
        }

    }

    {
        //TODO: create IBlockState for our IBakedModel and figure out how to insert both into the system
    }

    // TODO: Maybe use a tessellator instead?
    public static void render(RenderInfo renderInfo) {
        if (renderInfo.glow)
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 210F, 210F);

        //TODO: generate BakedQuads

//        GlStateManager.pushMatrix();
//        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, brightness.apply(partialTicks));
//        GlStateManager.popMatrix();
    }
}
