/*
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.client.particles;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class ParticleSimple extends ParticleBase {

    public double gravity = 0.004D;

    public ParticleSimple(World world, Vec3d start) {
        this(world, start, new Vec3d(0, 0, 0), 3f);
    }

    public ParticleSimple(World world, Vec3d start, Vec3d vel, float scale) {
        super(world, start, new Vec3d(0, 0, 0));
        this.motionX *= 0.1;
        this.motionY *= 0.1;
        this.motionZ *= 0.1;
        this.motionX += vel.xCoord;
        this.motionY += vel.yCoord;
        this.motionZ += vel.zCoord;
        this.particleScale *= 0.75F;
        this.particleScale *= scale;
        this.particleMaxAge = (int) (24.0D / (Math.random() * 0.5D + 0.2D));
        this.particleMaxAge = (int) (particleMaxAge * scale);
        this.noClip = true;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        this.prevPosX = posX;
        this.prevPosY = posY;
        this.prevPosZ = posZ;

        if (particleAge >= particleMaxAge)
            setExpired();
        this.particleAge++;

        setParticleTextureIndex(7 - particleAge * 8 / particleMaxAge);
        this.motionY += gravity;
        moveEntity(motionX, motionY, motionZ);

        if (posY == prevPosY) {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
        }

        this.motionX *= 0.96D;
        this.motionY *= 0.96D;
        this.motionZ *= 0.96D;

        if (isCollided) {
            this.motionX *= 0.67D;
            this.motionZ *= 0.67D;
        }
    }

}
