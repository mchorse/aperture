package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.values.ValueAngle;

/**
 * Angle modifier
 * 
 * This camera modifier simply adds stored angle values to given 
 * position. 
 */
public class AngleModifier extends AbstractModifier
{
    public final ValueAngle angle = new ValueAngle("angle", new Angle(0, 0, 0, 0));

    public AngleModifier()
    {
        super();

        this.register(this.angle);
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        Angle angle = this.angle.get();

        pos.angle.yaw += angle.yaw;
        pos.angle.pitch += angle.pitch;
        pos.angle.roll += angle.roll;
        pos.angle.fov += angle.fov;
    }

    @Override
    public AbstractModifier create()
    {
        return new AngleModifier();
    }
}