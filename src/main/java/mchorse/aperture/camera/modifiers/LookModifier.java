package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import net.minecraft.util.math.MathHelper;

/**
 * Look modifier
 * 
 * This modifier locks fixture's angle so it would always look in the 
 * direction of entity. Relative yaw and pitch is also supported.
 */
public class LookModifier extends EntityModifier
{
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

        double x = (this.entity.lastTickPosX + (this.entity.posX - this.entity.lastTickPosX) * partialTick);
        double y = (this.entity.lastTickPosY + (this.entity.posY - this.entity.lastTickPosY) * partialTick);
        double z = (this.entity.lastTickPosZ + (this.entity.posZ - this.entity.lastTickPosZ) * partialTick);

        double dX = x - pos.point.x;
        double dY = y - pos.point.y;
        double dZ = z - pos.point.z;
        double horizontalDistance = MathHelper.sqrt(dX * dX + dZ * dZ);

        float yaw = (float) (MathHelper.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(dY, horizontalDistance) * (180D / Math.PI)));

        yaw += pos.angle.yaw - this.position.angle.yaw;
        pitch += pos.angle.pitch - this.position.angle.pitch;

        pos.angle.set(yaw, pitch);
    }
}