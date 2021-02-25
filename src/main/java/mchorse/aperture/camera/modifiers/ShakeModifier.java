package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.config.values.ValueFloat;

/**
 * Shake modifier
 * 
 * This modifier shakes the camera depending on the given component 
 * flags.
 */
public class ShakeModifier extends ComponentModifier
{
    public final ValueFloat shake = new ValueFloat("shake");
    public final ValueFloat shakeAmount = new ValueFloat("shakeAmount");

    public ShakeModifier()
    {
        super();

        this.register(this.shake);
        this.register(this.shakeAmount);
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        float shake = this.shake.get();
        float amount = this.shakeAmount.get();
        float x = (ticks + previewPartialTick) / (shake == 0 ? 1 : shake);

        boolean isX = this.isActive(0);
        boolean isY = this.isActive(1);
        boolean isZ = this.isActive(2);
        boolean isYaw = this.isActive(3);
        boolean isPitch = this.isActive(4);
        boolean isRoll = this.isActive(5);
        boolean isFov = this.isActive(6);

        if (isYaw && isPitch && !isX && !isY && !isZ && !isRoll && !isFov)
        {
            float swingX = (float) (Math.sin(x) * Math.sin(x) * Math.cos(x) * Math.cos(x / 2));
            float swingY = (float) (Math.cos(x) * Math.sin(x) * Math.sin(x));

            pos.angle.yaw += swingX * amount;
            pos.angle.pitch += swingY * amount;
        }
        else
        {
            if (isX)
            {
                pos.point.x += Math.sin(x) * amount;
            }

            if (isY)
            {
                pos.point.y -= Math.sin(x) * amount;
            }

            if (isZ)
            {
                pos.point.z += Math.cos(x) * amount;
            }

            if (isYaw)
            {
                pos.angle.yaw += Math.sin(x) * amount;
            }

            if (isPitch)
            {
                pos.angle.pitch += Math.cos(x) * amount;
            }

            if (isRoll)
            {
                pos.angle.roll += Math.sin(x) * amount;
            }

            if (isFov)
            {
                pos.angle.fov += Math.cos(x) * amount;
            }
        }
    }

    @Override
    public AbstractModifier create()
    {
        return new ShakeModifier();
    }
}