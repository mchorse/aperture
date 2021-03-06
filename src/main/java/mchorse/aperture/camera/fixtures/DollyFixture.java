package mchorse.aperture.camera.fixtures;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.values.ValueInterpolation;
import mchorse.mclib.config.values.ValueFloat;
import mchorse.mclib.utils.Interpolation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DollyFixture extends IdleFixture
{
    public final ValueFloat distance = new ValueFloat("distance", 0.1F);
    public final ValueInterpolation interp = new ValueInterpolation("interp");
    public final ValueFloat yaw = new ValueFloat("yaw", 0);
    public final ValueFloat pitch = new ValueFloat("pitch", 0);

    public DollyFixture(long duration)
    {
        super(duration);

        this.register(this.distance);
        this.register(this.interp);
        this.register(this.yaw);
        this.register(this.pitch);
    }

    @Override
    public void fromPlayer(EntityPlayer player)
    {
        super.fromPlayer(player);

        this.yaw.set(this.position.get().angle.yaw);
        this.pitch.set(this.position.get().angle.pitch);
    }

    @Override
    public void applyFixture(long ticks, float partialTicks, float previewPartialTick, CameraProfile profile, Position pos)
    {
        pos.copy(this.position.get());

        Interpolation interp = this.interp.get();
        Point point = this.position.get().point;
        double x = point.x;
        double y = point.y;
        double z = point.z;

        final float degToPi = (float) Math.PI / 180;

        float yaw = this.yaw.get();
        float pitch = this.pitch.get();
        float cos = MathHelper.cos(-yaw * degToPi - (float) Math.PI);
        float sin = MathHelper.sin(-yaw * degToPi - (float) Math.PI);
        float cos2 = -MathHelper.cos(-pitch * degToPi);
        float sin2 = MathHelper.sin(-pitch * degToPi);
        Vec3d look = new Vec3d(sin * cos2, sin2, cos * cos2).normalize().scale(this.distance.get());

        partialTicks = (ticks + previewPartialTick) / this.getDuration();

        x = interp.interpolate(x, x + look.x, partialTicks);
        y = interp.interpolate(y, y + look.y, partialTicks);
        z = interp.interpolate(z, z + look.z, partialTicks);

        pos.point.set(x, y, z);
    }

    @Override
    public AbstractFixture create(long duration)
    {
        return new DollyFixture(duration);
    }

    @Override
    public void copyByReplacing(AbstractFixture from)
    {
        super.copyByReplacing(from);

        if (from instanceof PathFixture)
        {
            PathFixture path = (PathFixture) from;

            if (path.size() != 2)
            {
                return;
            }

            Position a = path.getPoint(0);
            Position b = path.getPoint(1);
            Angle angle = Angle.angle(b.point, a.point);

            this.distance.set((float) a.point.length(b.point));
            this.position.get().copy(a);

            this.yaw.set(angle.yaw);
            this.pitch.set(angle.pitch);

            Interpolation function = path.interpolation.get().function;

            if (function != null)
            {
                this.interp.set(function);
            }
        }
    }

    @Override
    protected void breakDownFixture(AbstractFixture original, long offset)
    {
        super.breakDownFixture(original, offset);

        DollyFixture dolly = (DollyFixture) original;
        Position position = new Position();

        original.applyFixture(offset, 0, 0, null, position);

        Point point = dolly.position.get().point;
        double dx = point.x - position.point.x;
        double dy = point.y - position.point.y;
        double dz = point.z - position.point.z;
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        this.position.set(position);
        this.distance.set(dolly.distance.get() - distance);
        dolly.distance.set(distance);
    }
}