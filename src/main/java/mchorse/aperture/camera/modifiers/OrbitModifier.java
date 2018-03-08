package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Orbit modifier
 * 
 * This modifier is quite similar to {@link LookModifier} and 
 * {@link FollowModifier}, but kind of combination of both of them. It 
 * is responsible for making the camera orbit around the given entity 
 * with given yaw and pitch. 
 */
public class OrbitModifier extends EntityModifier
{
    /**
     * Yaw to be added to orbit
     */
    @Expose
    public float yaw;

    /**
     * Pitch to be added to orbit
     */
    @Expose
    public float pitch;

    /**
     * How far away to orbit from the entity
     */
    @Expose
    public float distance;

    /**
     * In addition, copy yaw and pitch from entity
     */
    @Expose
    public boolean copy;

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, Position pos)
    {
        if (this.entity == null || this.entity.isDead)
        {
            this.tryFindingEntity();
        }

        if (this.entity == null)
        {
            return;
        }

        fixture.applyFixture(0, 0, this.position);

        float yaw = 0;
        float pitch = 0;
        float distance = this.distance;

        /* Copy entity's yaw and pitch */
        if (this.copy)
        {
            yaw = this.entity.prevRotationYaw + (this.entity.rotationYaw - this.entity.prevRotationYaw) * partialTick;
            pitch = this.entity.prevRotationPitch + (this.entity.rotationPitch - this.entity.prevRotationPitch) * partialTick;

            if (entity instanceof EntityLivingBase)
            {
                EntityLivingBase mob = (EntityLivingBase) entity;

                yaw = mob.prevRotationYawHead + (mob.rotationYawHead - mob.prevRotationYawHead) * partialTick;
            }
        }

        float oldYaw = yaw;

        /* Add relative and stored yaw, pitch and distance */
        yaw += this.yaw;
        yaw += pos.angle.yaw - this.position.angle.yaw;

        if (this.copy)
        {
            double factor = Math.abs((Math.abs(oldYaw - yaw) % 360) / 360 - 0.5) * 4 - 1;

            pitch *= factor;
        }

        pitch += this.pitch;
        pitch += pos.angle.pitch - this.position.angle.pitch;

        distance += pos.point.z - this.position.point.z;

        /* Calculate entity's position */
        float x = (float) (this.entity.lastTickPosX + (this.entity.posX - this.entity.lastTickPosX) * partialTick);
        float y = (float) (this.entity.lastTickPosY + (this.entity.posY - this.entity.lastTickPosY) * partialTick);
        float z = (float) (this.entity.lastTickPosZ + (this.entity.posZ - this.entity.lastTickPosZ) * partialTick);

        /* Calculate look vector */
        final float degToPi = (float) Math.PI / 180;

        float f = MathHelper.cos(-yaw * degToPi - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * degToPi - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * degToPi);
        float f3 = MathHelper.sin(-pitch * degToPi);
        Vec3d look = new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));

        pos.point.set(x, y, z);

        /* Add to entity's position orbit offset */
        x += look.x * distance;
        y += look.y * distance;
        z += look.z * distance;

        /* Look at the origin */
        double dX = pos.point.x - x;
        double dY = pos.point.y - y;
        double dZ = pos.point.z - z;
        double horizontalDistance = MathHelper.sqrt(dX * dX + dZ * dZ);

        yaw = (float) (MathHelper.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
        pitch = (float) (-(MathHelper.atan2(dY, horizontalDistance) * (180D / Math.PI)));

        pos.point.set(x, y, z);
        pos.angle.set(yaw, pitch);
    }

    @Override
    public AbstractModifier clone()
    {
        OrbitModifier modifier = new OrbitModifier();

        modifier.enabled = this.enabled;
        modifier.selector = this.selector;
        modifier.yaw = this.yaw;
        modifier.pitch = this.pitch;
        modifier.distance = this.distance;
        modifier.copy = this.copy;

        return modifier;
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.yaw = buffer.readFloat();
        this.pitch = buffer.readFloat();
        this.distance = buffer.readFloat();
        this.copy = buffer.readBoolean();
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        buffer.writeFloat(this.yaw);
        buffer.writeFloat(this.pitch);
        buffer.writeFloat(this.distance);
        buffer.writeBoolean(this.copy);
    }
}