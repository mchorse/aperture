package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
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
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, Position pos)
    {
        float x = (ticks + partialTick) / (this.shake == 0 ? 1 : this.shake);

        float swingX = (float) (Math.sin(x) * Math.sin(x) * Math.cos(x));
        float swingY = (float) (Math.cos(x) * Math.sin(x) * Math.sin(x));

        pos.angle.yaw += swingX * this.shakeAmount;
        pos.angle.pitch += swingY * this.shakeAmount;
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