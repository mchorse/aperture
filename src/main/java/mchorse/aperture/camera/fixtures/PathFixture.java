package mchorse.aperture.camera.fixtures;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.Aperture;
import mchorse.aperture.camera.Angle;
import mchorse.aperture.camera.Point;
import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.smooth.Interpolations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

/**
 * Path camera fixture
 *
 * This fixture is responsible for making smooth camera movements.
 */
public class PathFixture extends AbstractFixture
{
    @Expose
    public boolean perPointDuration;

    @Expose
    protected List<DurablePosition> points = new ArrayList<DurablePosition>();

    public InterpolationType interpolationPos;
    public InterpolationType interpolationAngle;

    public PathFixture(long duration)
    {
        super(duration);

        InterpolationType type = interpFromString(Aperture.proxy.config.camera_path_default_interp);

        this.interpolationPos = type;
        this.interpolationAngle = type;
    }

    public DurablePosition getPoint(int index)
    {
        if (this.points.size() == 0)
        {
            return new DurablePosition(0, 0, 0, 0, 0);
        }

        if (index >= this.points.size())
        {
            return this.points.get(this.points.size() - 1);
        }

        if (index < 0)
        {
            return this.points.get(0);
        }

        return this.points.get(index);
    }

    public boolean hasPoint(int index)
    {
        return !this.points.isEmpty() && index >= 0 && index < this.points.size();
    }

    public List<DurablePosition> getPoints()
    {
        return this.points;
    }

    public int getCount()
    {
        return this.points.size();
    }

    public void addPoint(DurablePosition point)
    {
        this.points.add(point);
    }

    public void addPoint(DurablePosition point, int before)
    {
        this.points.add(before, point);
    }

    public void movePoint(int from, int to)
    {
        this.points.add(to, this.points.remove(from));
    }

    public void editPoint(DurablePosition point, int index)
    {
        this.points.set(index, point);
    }

    public void removePoint(int index)
    {
        this.points.remove(index);
    }

    @Override
    public long getDuration()
    {
        if (this.perPointDuration)
        {
            long duration = 0;

            for (DurablePosition pos : this.points)
            {
                duration += pos.duration;
            }

            return duration;
        }

        return super.getDuration();
    }

    /**
     * Return index of a point at given frame (relative to that path fixture, i.e. 0)  
     */
    public int getIndexForPoint(int frame)
    {
        if (!this.perPointDuration)
        {
            float range = (float) frame / this.duration;
            int index = (int) Math.floor(range * (this.points.size() - 1));

            return MathHelper.clamp(index, 0, (int) this.duration);
        }

        int index = 0;
        long point = 0;

        while (point < frame)
        {
            point += this.points.get(index).getDuration();

            if (point >= frame)
            {
                break;
            }

            index++;
        }

        return MathHelper.clamp(index, 0, this.getCount() - 1);
    }

    /**
     * Return the frame for point at the index   
     */
    public long getTickForPoint(int index)
    {
        if (!this.perPointDuration)
        {
            return (long) ((index / (float) (this.points.size() - 1)) * this.duration);
        }

        long duration = 0;

        for (DurablePosition pos : this.points)
        {
            index--;

            if (index < 0)
            {
                break;
            }

            duration += pos.getDuration();
        }

        return duration;
    }

    @Override
    public void edit(String[] args, EntityPlayer player) throws CommandException
    {
        if (args.length == 0)
        {
            this.addPoint(new DurablePosition(player));
        }
        else if (args.length == 1)
        {
            if (args[0].equals("linear") || args[0].equals("cubic") || args[0].equals("hermite"))
            {
                InterpolationType type = interpFromString(args[0]);

                this.interpolationPos = type;
                this.interpolationAngle = type;
            }
            else
            {
                int max = this.points.size() - 1;

                this.points.get(CommandBase.parseInt(args[0], 0, max)).set(player);
            }
        }
        else if (args.length == 2)
        {
            if (args[0].equals("linear") || args[0].equals("cubic") || args[0].equals("hermite"))
            {
                this.interpolationPos = interpFromString(args[0]);
            }

            if (args[1].equals("linear") || args[1].equals("cubic") || args[1].equals("hermite"))
            {
                this.interpolationAngle = interpFromString(args[1]);
            }
        }
    }

    @Override
    public void applyFixture(float progress, float partialTicks, Position pos)
    {
        long duration = this.getDuration();

        if (this.points.isEmpty() || duration == 0)
        {
            return;
        }

        int length = this.points.size() - 1;
        int index = 0;

        float x = progress + (1.0F / duration) * partialTicks;

        if (this.perPointDuration)
        {
            long frame = (long) (progress * duration);
            int points = this.points.size();

            long point = 0;
            long prevPoint = 0;

            while (point - 1 < frame)
            {
                if (index >= points)
                {
                    break;
                }

                prevPoint = point;
                point += this.points.get(index).getDuration();

                if (point - 1 >= frame)
                {
                    break;
                }

                index++;
            }

            if (index < points - 1)
            {
                float diff = point - prevPoint;

                x = (float) ((frame + partialTicks) - prevPoint) / (diff == 0 ? 1.0F : diff);
            }
            else
            {
                index = points - 1;
                x = 0;
            }

            index = MathHelper.clamp(index, 0, points - 1);
        }
        else
        {
            x = MathHelper.clamp(x * length, 0, length);
            index = (int) Math.floor(x);
            x = x - index;
        }

        this.apply(pos, index, x);
    }

    /**
     * Apply interpolation and stuff
     *
     * Basic if-else, because I'm really lazy to write clever implementation,
     * lol.
     */
    private void apply(Position pos, int index, float progress)
    {
        float x, y, z;
        float yaw, pitch, roll, fov;

        if (this.interpolationPos == null)
        {
            this.interpolationPos = InterpolationType.LINEAR;
        }

        if (this.interpolationAngle == null)
        {
            this.interpolationAngle = InterpolationType.LINEAR;
        }

        Position p0 = this.getPoint(index - 1);
        Position p1 = this.getPoint(index);
        Position p2 = this.getPoint(index + 1);
        Position p3 = this.getPoint(index + 2);

        /* Interpolating the angle */
        if (this.interpolationAngle.equals(InterpolationType.CUBIC))
        {
            yaw = Interpolations.cubicYaw(p0.angle.yaw, p1.angle.yaw, p2.angle.yaw, p3.angle.yaw, progress);
            pitch = Interpolations.cubic(p0.angle.pitch, p1.angle.pitch, p2.angle.pitch, p3.angle.pitch, progress);
            roll = Interpolations.cubic(p0.angle.roll, p1.angle.roll, p2.angle.roll, p3.angle.roll, progress);
            fov = Interpolations.cubic(p0.angle.fov, p1.angle.fov, p2.angle.fov, p3.angle.fov, progress);
        }
        else if (this.interpolationAngle.equals(InterpolationType.HERMITE))
        {
            yaw = (float) Interpolations.cubicHermiteYaw(p0.angle.yaw, p1.angle.yaw, p2.angle.yaw, p3.angle.yaw, progress);
            pitch = (float) Interpolations.cubicHermite(p0.angle.pitch, p1.angle.pitch, p2.angle.pitch, p3.angle.pitch, progress);
            roll = (float) Interpolations.cubicHermite(p0.angle.roll, p1.angle.roll, p2.angle.roll, p3.angle.roll, progress);
            fov = (float) Interpolations.cubicHermite(p0.angle.fov, p1.angle.fov, p2.angle.fov, p3.angle.fov, progress);
        }
        else
        {
            yaw = Interpolations.lerpYaw(p1.angle.yaw, p2.angle.yaw, progress);
            pitch = Interpolations.lerp(p1.angle.pitch, p2.angle.pitch, progress);
            roll = Interpolations.lerp(p1.angle.roll, p2.angle.roll, progress);
            fov = Interpolations.lerp(p1.angle.fov, p2.angle.fov, progress);
        }

        /* Interpolating the position */
        if (this.interpolationPos.equals(InterpolationType.CUBIC))
        {
            x = Interpolations.cubic(p0.point.x, p1.point.x, p2.point.x, p3.point.x, progress);
            y = Interpolations.cubic(p0.point.y, p1.point.y, p2.point.y, p3.point.y, progress);
            z = Interpolations.cubic(p0.point.z, p1.point.z, p2.point.z, p3.point.z, progress);
        }
        else if (this.interpolationPos.equals(InterpolationType.HERMITE))
        {
            x = (float) Interpolations.cubicHermite(p0.point.x, p1.point.x, p2.point.x, p3.point.x, progress);
            y = (float) Interpolations.cubicHermite(p0.point.y, p1.point.y, p2.point.y, p3.point.y, progress);
            z = (float) Interpolations.cubicHermite(p0.point.z, p1.point.z, p2.point.z, p3.point.z, progress);
        }
        else
        {
            x = Interpolations.lerp(p1.point.x, p2.point.x, progress);
            y = Interpolations.lerp(p1.point.y, p2.point.y, progress);
            z = Interpolations.lerp(p1.point.z, p2.point.z, progress);
        }

        pos.point.set(x, y, z);
        pos.angle.set(yaw, pitch, roll, fov);
    }

    /* Save/load methods */

    @Override
    public byte getType()
    {
        return AbstractFixture.PATH;
    }

    @Override
    public void toJSON(JsonObject object)
    {
        object.addProperty("interpolation", this.interpolationPos.name);
        object.addProperty("interpolationAngle", this.interpolationAngle.name);
    }

    @Override
    public void fromJSON(JsonObject object)
    {
        if (object.has("interpolation"))
        {
            this.interpolationPos = interpFromString(object.get("interpolation").getAsString());
        }

        if (object.has("interpolationAngle"))
        {
            this.interpolationAngle = interpFromString(object.get("interpolationAngle").getAsString());
        }
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.perPointDuration = buffer.readBoolean();
        this.interpolationPos = interpFromInt(buffer.readByte());
        this.interpolationAngle = interpFromInt(buffer.readByte());

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            this.addPoint(DurablePosition.fromByteBuf(buffer));
        }
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        buffer.writeBoolean(this.perPointDuration);
        buffer.writeByte(this.interpolationPos.id);
        buffer.writeByte(this.interpolationAngle.id);

        buffer.writeInt(this.points.size());

        for (DurablePosition pos : this.points)
        {
            pos.toByteBuf(buffer);
        }
    }

    /* Interpolation */

    public static InterpolationType interpFromInt(int number)
    {
        if (number == 1)
        {
            return InterpolationType.CUBIC;
        }

        if (number == 2)
        {
            return InterpolationType.HERMITE;
        }

        return InterpolationType.LINEAR;
    }

    public static InterpolationType interpFromString(String string)
    {
        if (string.equals("cubic"))
        {
            return InterpolationType.CUBIC;
        }

        if (string.equals("hermite"))
        {
            return InterpolationType.HERMITE;
        }

        return InterpolationType.LINEAR;
    }

    public static enum InterpolationType
    {
        LINEAR("linear", 0), CUBIC("cubic", 1), HERMITE("hermite", 2);

        public final String name;
        public final int id;

        private InterpolationType(String name, int id)
        {
            this.name = name;
            this.id = id;
        }
    }

    /**
     * Durable position
     * 
     * Just like a position, but with a duration
     */
    public static class DurablePosition extends Position
    {
        @Expose
        protected long duration = 1L;

        public static DurablePosition fromByteBuf(ByteBuf buffer)
        {
            return new DurablePosition(buffer.readLong(), Point.fromByteBuf(buffer), Angle.fromByteBuf(buffer));
        }

        public DurablePosition(EntityPlayer player)
        {
            super(player);
        }

        public DurablePosition(Point point, Angle angle)
        {
            super(point, angle);
        }

        public DurablePosition(long duration, Point point, Angle angle)
        {
            super(point, angle);

            this.setDuration(duration);
        }

        public DurablePosition(float x, float y, float z, float yaw, float pitch)
        {
            super(x, y, z, yaw, pitch);
        }

        public long getDuration()
        {
            return this.duration;
        }

        public void setDuration(long duration)
        {
            this.duration = duration < 0 ? 0 : duration;
        }

        @Override
        public void toByteBuf(ByteBuf buffer)
        {
            buffer.writeLong(this.duration);

            super.toByteBuf(buffer);
        }
    }
}