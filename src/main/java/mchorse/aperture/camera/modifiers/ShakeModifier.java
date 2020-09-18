package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

/**
 * Shake modifier
 * 
 * This modifier shakes the camera depending on the given component 
 * flags.
 */
public class ShakeModifier extends ComponentModifier
{
    @Expose
    public float shake;

    @Expose
    public float shakeAmount;

    public ShakeModifier()
    {}

    public ShakeModifier(float shake, float shakeAmount)
    {
        this.shake = shake;
        this.shakeAmount = shakeAmount;
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        float x = (ticks + previewPartialTick) / (this.shake == 0 ? 1 : this.shake);

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

            pos.angle.yaw += swingX * this.shakeAmount;
            pos.angle.pitch += swingY * this.shakeAmount;
        }
        else
        {
            if (isX)
            {
                pos.point.x += Math.sin(x) * this.shakeAmount;
            }

            if (isY)
            {
                pos.point.y -= Math.sin(x) * this.shakeAmount;
            }

            if (isZ)
            {
                pos.point.z += Math.cos(x) * this.shakeAmount;
            }

            if (isYaw)
            {
                pos.angle.yaw += Math.sin(x) * this.shakeAmount;
            }

            if (isPitch)
            {
                pos.angle.pitch += Math.cos(x) * this.shakeAmount;
            }

            if (isRoll)
            {
                pos.angle.roll += Math.sin(x) * this.shakeAmount;
            }

            if (isFov)
            {
                pos.angle.fov += Math.cos(x) * this.shakeAmount;
            }
        }
    }

    @Override
    public AbstractModifier create()
    {
        return new ShakeModifier();
    }

    @Override
    public void copy(AbstractModifier from)
    {
        super.copy(from);

        if (from instanceof ShakeModifier)
        {
            ShakeModifier modifier = (ShakeModifier) from;

            this.shake = modifier.shake;
            this.shakeAmount = modifier.shakeAmount;
        }
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        buffer.writeFloat(this.shake);
        buffer.writeFloat(this.shakeAmount);
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.shake = buffer.readFloat();
        this.shakeAmount = buffer.readFloat();
    }
}