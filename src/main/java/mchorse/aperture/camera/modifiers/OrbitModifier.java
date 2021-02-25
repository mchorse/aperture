package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueFloat;
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
    public final ValueFloat yaw = new ValueFloat("yaw");

    /**
     * Pitch to be added to orbit
     */
    public final ValueFloat pitch = new ValueFloat("pitch");

    /**
     * How far away to orbit from the entity
     */
    public final ValueFloat distance = new ValueFloat("distance");

    /**
     * In addition, copy yaw and pitch from entity
     */
    public final ValueBoolean copy = new ValueBoolean("copy");

    public OrbitModifier()
    {
        super();

        this.register(this.yaw);
        this.register(this.pitch);
        this.register(this.distance);
        this.register(this.copy);
    }

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
            fixture.applyFixture(0, 0, 0, profile, this.position);
        }
        else
        {
            this.position.copy(pos);
        }

        float yaw = 0;
        float pitch = 0;
        float distance = this.distance.get();
        Entity entity = this.entities.get(0);

        /* Copy entity's yaw and pitch */
        if (this.copy.get())
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
        yaw += this.yaw.get();
        yaw += pos.angle.yaw - this.position.angle.yaw;

        if (this.copy.get())
        {
            pitch *= Math.abs((Math.abs(oldYaw - yaw) % 360) / 360 - 0.5) * 4 - 1;
        }

        pitch += this.pitch.get();
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
}