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
    public AbstractModifier create()
    {
        return new AngleModifier();
    }

    @Override
    public void copy(AbstractModifier from)
    {
        super.copy(from);

        if (from instanceof AngleModifier)
        {
            this.angle = ((AngleModifier) from).angle.copy();
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.angle.toBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.angle = Angle.fromBytes(buffer);
    }
}