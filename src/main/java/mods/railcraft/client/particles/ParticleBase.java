/*******************************************************************************
 * Copyright (c) CovertJaguar, 2011-2016
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/

package mods.railcraft.client.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Created by CovertJaguar on 5/29/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ParticleBase extends Particle {
    protected boolean noClip;
    protected boolean dimAsAge;

    public ParticleBase(World par1World, Vec3d start) {
        this(par1World, start, new Vec3d(0, 0, 0));
    }

    public ParticleBase(World par1World, Vec3d start, Vec3d vel) {
        super(par1World, start.xCoord, start.yCoord, start.zCoord, vel.xCoord, vel.yCoord, vel.zCoord);
    }

    public Vec3d getPos() {
        return new Vec3d(posX, posY, posZ);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(posX, posY + 0.5D, posZ);
    }

    @Override
    public int getBrightnessForRender(float par1) {
        if (dimAsAge) {
            int var2 = super.getBrightnessForRender(par1);
            float var3 = (float) particleAge / (float) particleMaxAge;
            var3 *= var3;
            var3 *= var3;
            int var4 = var2 & 255;
            int var5 = var2 >> 16 & 255;
            var5 += (int) (var3 * 15.0F * 16.0F);

            if (var5 > 240) {
                var5 = 240;
            }

            return var4 | var5 << 16;
        }
        return super.getBrightnessForRender(par1);
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        if (noClip) {
            setEntityBoundingBox(getEntityBoundingBox().offset(x, y, z));
            resetPositionToBB();
        } else {
            super.moveEntity(x, y, z);
        }
    }
}
