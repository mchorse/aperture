package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import net.minecraft.entity.Entity;
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

    public OrbitModifier()
    {}

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        if (this.checkForDead())
        {
            this.tryFindingEntity();
        }

        if (this.entities == null)
        {
            return;
        }

        if (fixture != null)
        {
            fixture.applyFixture(0, 0, previewPartialTick, profile, this.position);
        }
        else
        {
            this.position.copy(pos);
        }

        float yaw = 0;
        float pitch = 0;
        float distance = this.distance;
        Entity entity = this.entities.get(0);

        /* Copy entity's yaw and pitch */
        if (this.copy)
        {
            yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick;
            pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick;

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
            pitch *= Math.abs((Math.abs(oldYaw - yaw) % 360) / 360 - 0.5) * 4 - 1;
        }

        pitch += this.pitch;
        pitch += pos.angle.pitch - this.position.angle.pitch;

        distance += pos.point.z - this.position.point.z;

        /* Calculate entity's position */
        float x = (float) (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTick);
        float y = (float) (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTick);
        float z = (float) (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTick);

        /* Calculate look vector */
        final float degToPi = (float) Math.PI / 180;

        float f = MathHelper.cos(-yaw * degToPi - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * degToPi - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * degToPi);
        float f3 = MathHelper.sin(-pitch * degToPi);
        Vec3d look = new Vec3d(f1 * f2, f3, f * f2);

        pos.point.set(x, y, z);

        /* Add to entity's position orbit offset */
        x += look.xCoord * distance;
        y += look.yCoord * distance;
        z += look.zCoord * distance;

        /* Look at the origin */
        double dX = pos.point.x - x;
        double dY = pos.point.y - y;
        double dZ = pos.point.z - z;
        double horizontalDistance = MathHelper.sqrt_double(dX * dX + dZ * dZ);

        yaw = (float) (MathHelper.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
        pitch = (float) (-(MathHelper.atan2(dY, horizontalDistance) * (180D / Math.PI)));

        pos.point.set(x, y, z);
        pos.angle.set(yaw, pitch);
    }

    @Override
    public AbstractModifier copy()
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