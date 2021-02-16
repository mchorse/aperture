package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
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

        float cos = MathHelper.cos(-yaw * degToPi - (float) Math.PI);
        float sin = MathHelper.sin(-yaw * degToPi - (float) Math.PI);
        float cos2 = -MathHelper.cos(-pitch * degToPi);
        float sin2 = MathHelper.sin(-pitch * degToPi);
        Vec3d look = new Vec3d(sin * cos2, sin2, cos * cos2);

        pos.point.set(x, y, z);

        /* Add to entity's position orbit offset */
        x += look.x * distance;
        y += look.y * distance;
        z += look.z * distance;

        /* Look at the origin */
        double dX = pos.point.x - x;
        double dY = pos.point.y - y;
        double dZ = pos.point.z - z;
        Angle angle = Angle.angle(dX, dY, dZ);

        pos.point.set(x, y, z);
        pos.angle.set(angle.yaw, angle.pitch);
    }

    @Override
    public AbstractModifier create()
    {
        return new OrbitModifier();
    }

    @Override
    public void copy(AbstractModifier from)
    {
        super.copy(from);

        if (from instanceof OrbitModifier)
        {
            OrbitModifier modifier = (OrbitModifier) from;

            this.yaw = modifier.yaw;
            this.pitch = modifier.pitch;
            this.distance = modifier.distance;
            this.copy = modifier.copy;
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.yaw = buffer.readFloat();
        this.pitch = buffer.readFloat();
        this.distance = buffer.readFloat();
        this.copy = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeFloat(this.yaw);
        buffer.writeFloat(this.pitch);
        buffer.writeFloat(this.distance);
        buffer.writeBoolean(this.copy);
    }
}