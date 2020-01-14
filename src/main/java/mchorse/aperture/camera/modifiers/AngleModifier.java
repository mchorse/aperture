package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

/**
 * Angle modifier
 * 
 * This camera modifier simply adds stored angle values to given 
 * position. 
 */
public class AngleModifier extends AbstractModifier
{
    @Expose
    public Angle angle = new Angle(0, 0, 0, 0);

    public AngleModifier()
    {}

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        pos.angle.yaw += this.angle.yaw;
        pos.angle.pitch += this.angle.pitch;
        pos.angle.roll += this.angle.roll;
        pos.angle.fov += this.angle.fov;
    }

    @Override
    public AbstractModifier copy()
    {
        AngleModifier modifier = new AngleModifier();

        modifier.enabled = this.enabled;
        modifier.angle = this.angle.clone();

        return modifier;
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        this.angle.toByteBuf(buffer);
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.angle = Angle.fromByteBuf(buffer);
    }
}