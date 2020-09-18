package mchorse.aperture.camera.fixtures;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
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
    @Expose
    public Point start = new Point(0, 0, 0);

    /**
     * Start angle offset (in degrees)
     */
    @Expose
    public float offset = 0;

    /**
     * Distance (in blocks units) from center point
     */
    @Expose
    public float distance = 5;

    /**
     * How much degrees to perform during running
     */
    @Expose
    public float circles = 360;

    /**
     * Pitch of the circular fixture
     */
    @Expose
    public float pitch = 0;

    public CircularFixture(long duration)
    {
        super(duration);
    }

    @Override
    public void fromPlayer(EntityPlayer player)
    {
        this.start.set(player);
        this.pitch = player.rotationPitch;
    }

    @Override
    public void applyFixture(long ticks, float partialTicks, float previewPartialTick, CameraProfile profile, Position pos)
    {
        float progress = (ticks / (float) this.duration) + (1.0F / this.duration * previewPartialTick);
        float angle = (float) (Math.toRadians(this.offset) + progress * Math.toRadians(this.circles));

        double cos = this.distance * Math.cos(angle);
        double sin = this.distance * Math.sin(angle);

        /* +0.5, because player's position isn't in the entity's center */
        double x = this.start.x + 0.5 + cos;
        double y = this.start.y;
        double z = this.start.z + 0.5 + sin;

        float yaw = (float) (MathHelper.atan2(sin, cos) * (180D / Math.PI)) - 90.0F;

        pos.point.set(x - 0.5F, y, z - 0.5F);
        pos.angle.set(MathHelper.wrapDegrees(yaw - 180.0F), this.pitch, 0, 70);
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

            this.start = circular.start.copy();
            this.offset = circular.offset;
            this.distance = circular.distance;
            this.circles = circular.circles;
            this.pitch = circular.pitch;
        }
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.start = Point.fromByteBuf(buffer);
        this.offset = buffer.readFloat();
        this.distance = buffer.readFloat();
        this.circles = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        this.start.toByteBuf(buffer);
        buffer.writeFloat(this.offset);
        buffer.writeFloat(this.distance);
        buffer.writeFloat(this.circles);
        buffer.writeFloat(this.pitch);
    }
}