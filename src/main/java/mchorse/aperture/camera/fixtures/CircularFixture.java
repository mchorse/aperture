package mchorse.aperture.camera.fixtures;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.values.ValuePoint;
import mchorse.mclib.config.values.ValueFloat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

/**
 * Circular camera fixture
 *
 * This camera fixture is responsible for rotating the camera so it would go
 * round in circles relatively given point in space.
 *
 * You know, like one of these rotating thingies on car expos that rotate cars
 * round and round and round...
 */
public class CircularFixture extends AbstractFixture
{
    /**
     * Center point of circular fixture
     */
    public final ValuePoint start = new ValuePoint("start", new Point(0, 0, 0));

    /**
     * Start angle offset (in degrees)
     */
    public final ValueFloat offset = new ValueFloat("offset", 0);

    /**
     * Distance (in blocks units) from center point
     */
    public final ValueFloat distance = new ValueFloat("distance", 5);

    /**
     * How much degrees to perform during running
     */
    public final ValueFloat circles = new ValueFloat("circles", 360);

    /**
     * Pitch of the circular fixture
     */
    public final ValueFloat pitch = new ValueFloat("pitch", 0);

    public CircularFixture(long duration)
    {
        super(duration);

        this.register(this.start);
        this.register(this.offset);
        this.register(this.distance);
        this.register(this.circles);
        this.register(this.pitch);
    }

    @Override
    public void fromPlayer(EntityPlayer player)
    {
        this.start.get().set(player);
        this.pitch.set(player.rotationPitch);
    }

    @Override
    public void applyFixture(long ticks, float partialTicks, float previewPartialTick, CameraProfile profile, Position pos)
    {
        float progress = (ticks / (float) this.getDuration()) + (1.0F / this.getDuration() * previewPartialTick);
        float angle = (float) (Math.toRadians(this.offset.get()) + progress * Math.toRadians(this.circles.get()));

        float distance = this.distance.get();
        double cos = distance * Math.cos(angle);
        double sin = distance * Math.sin(angle);

        /* +0.5, because player's position isn't in the entity's center */
        Point point = this.start.get();
        double x = point.x + 0.5 + cos;
        double y = point.y;
        double z = point.z + 0.5 + sin;

        float yaw = (float) (MathHelper.atan2(sin, cos) * (180D / Math.PI)) - 90.0F;

        pos.point.set(x - 0.5F, y, z - 0.5F);
        pos.angle.set(MathHelper.wrapDegrees(yaw - 180.0F), this.pitch.get(), 0, 70);
    }

    @Override
    public AbstractFixture create(long duration)
    {
        return new CircularFixture(duration);
    }

    @Override
    public void copy(AbstractFixture from)
    {
        super.copy(from);

        if (from instanceof CircularFixture)
        {
            CircularFixture circular = (CircularFixture) from;

            this.start.copy(circular.start);
            this.offset.copy(circular.offset);
            this.distance.copy(circular.distance);
            this.circles.copy(circular.circles);
            this.pitch.copy(circular.pitch);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.start.fromBytes(buffer);
        this.offset.fromBytes(buffer);
        this.distance.fromBytes(buffer);
        this.circles.fromBytes(buffer);
        this.pitch.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.start.toBytes(buffer);
        this.offset.toBytes(buffer);
        this.distance.toBytes(buffer);
        this.circles.toBytes(buffer);
        this.pitch.toBytes(buffer);
    }
}