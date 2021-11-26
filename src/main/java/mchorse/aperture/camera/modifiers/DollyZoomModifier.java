package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.config.values.ValueFloat;

public class DollyZoomModifier extends AbstractModifier
{
    public Position position = new Position(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

    public final ValueFloat focus = new ValueFloat("focus");

    public DollyZoomModifier()
    {
        super();

        this.register(this.focus);
    }

    @Override
    public AbstractModifier create()
    {
        return new DollyZoomModifier();
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        if (fixture == null)
        {
            return;
        }

        fixture.applyFixture(0, 0F, profile, this.position);

        double dist = this.focus.get() - this.focus.get() * Math.tan(Math.toRadians(this.position.angle.fov / 2.0)) / Math.tan(Math.toRadians(pos.angle.fov / 2.0));

        pos.point.x += dist * Math.cos(Math.toRadians(pos.angle.pitch)) * Math.sin(Math.toRadians(-pos.angle.yaw));
        pos.point.y -= dist * Math.sin(Math.toRadians(pos.angle.pitch));
        pos.point.z += dist * Math.cos(Math.toRadians(pos.angle.pitch)) * Math.cos(Math.toRadians(-pos.angle.yaw));
    }
}
