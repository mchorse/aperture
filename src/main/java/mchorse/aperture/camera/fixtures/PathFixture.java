package mchorse.aperture.camera.fixtures;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.Aperture;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeInterpolation;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

/**
 * Path camera fixture
 *
 * This fixture is responsible for making smooth camera movements.
 */
public class PathFixture extends AbstractFixture
{
    /**
     * Whether per point duration is active 
     */
    @Expose
    public boolean perPointDuration;

    /**
     * List of points in this fixture 
     */
    @Expose
    protected List<DurablePosition> points = new ArrayList<DurablePosition>();

    /**
     * Whether keyframe-able speed should be used for this
     */
    @Expose
    public boolean useSpeed;

    /**
     * Keyframe-able speed
     */
    @Expose
    public KeyframeChannel speed;

    public InterpolationType interpolationPos;
    public InterpolationType interpolationAngle;

    /* Speed related cache data */
    private float lastTick;
    private Point lastPoint = new Point(0, 0, 0);
    private Point tmpPoint = new Point(0, 0, 0);

    public PathFixture()
    {
        super(0);

        this.speed = new KeyframeChannel();
    }

    public PathFixture(long duration)
    {
        super(duration);

        InterpolationType type = interpFromString(Aperture.proxy.config.camera_path_default_interp);

        this.interpolationPos = type;
        this.interpolationAngle = type;
        this.speed = new KeyframeChannel();
        this.speed.insert(0, 1);
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

            return MathHelper.clamp_int(index, 0, (int) this.duration);
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

        return MathHelper.clamp_int(index, 0, this.getCount() - 1);
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
    public void fromPlayer(EntityPlayer player)
    {
        this.addPoint(new DurablePosition(player));
    }

    @Override
    public void applyFixture(long ticks, float partialTicks, CameraProfile profile, Position pos)
    {
        long duration = this.getDuration();

        if (this.points.isEmpty() || duration == 0)
        {
            return;
        }

        int length = this.points.size() - 1;
        int index = 0;
        float x = 0;

        if (this.perPointDuration)
        {
            int points = this.points.size();

            long point = 0;
            long prevPoint = 0;

            while (point - 1 < ticks)
            {
                if (index >= points)
                {
                    break;
                }

                prevPoint = point;
                point += this.points.get(index).getDuration();

                if (point - 1 >= ticks)
                {
                    break;
                }

                index++;
            }

            if (index < points - 1)
            {
                float diff = point - prevPoint;

                x = ((ticks + partialTicks) - prevPoint) / (diff == 0 ? 1.0F : diff);
            }
            else
            {
                index = points - 1;
                x = 0;
            }

            index = MathHelper.clamp_int(index, 0, points - 1);
        }
        else
        {
            x = (ticks / (float) this.duration) + (1.0F / duration) * partialTicks;
            x = MathHelper.clamp_float(x * length, 0, length);
            index = (int) Math.floor(x);
            x = x - index;
        }

        /* If use speed is enabled */
        if (this.useSpeed)
        {
            float tick = ticks + partialTicks;

            /* Just calculate enough for the speed for the difference */
            if (tick != this.lastTick || tick == 0)
            {
                this.applyPoint(this.lastPoint, 0, 0);
                this.recalculate(tick, pos.angle);
                pos.point.set(this.lastPoint.x, this.lastPoint.y, this.lastPoint.z);
            }
            else
            {
                pos.point.set(this.lastPoint.x, this.lastPoint.y, this.lastPoint.z);
            }

            this.lastTick = tick;
        }
        else
        {
            this.applyAngle(pos.angle, index, x);
            this.applyPoint(pos.point, index, x);
        }
    }

    /**
     * Recalculate the point and given angle based on the keyframe-able 
     * constant speed feature.
     * 
     * This is a quite fascinating piece of code. Hopefully, the 
     * comments below will help you  
     */
    private void recalculate(float tick, Angle angle)
    {
        /* Calculate the distance which must be reached at given tick 
         * (the target distance may exceed path's distance, see more 
         * comments below) */
        float target = 0F;

        for (int i = 0, c = (int) tick; i < c; i++)
        {
            target += this.speed.interpolate(i);
        }

        target += this.speed.interpolate(tick) * (tick % 1);
        target /= 20F;

        /* Try to calculate the actual distance traveled.
         * 
         * The loop below doesn't yield *exact* position based on ticks  
         * but rather an approximation. It starts from beginning, and 
         * tries to calculate the point that is close enough to the 
         * target */
        int index = 0;
        int size = this.points.size() - 1;
        float progress = 0;
        float distance = 0;
        float factor = 0.1F;
        float diff = Math.abs(target - distance);

        while (diff > 0.00005F)
        {
            progress += factor;

            /* To avoid infinite loop, we break things here. Factor with 
             * every iteration is definitely getting smaller */
            if (factor == 0 || Math.abs(factor) < 0.0000001F)
            {
                this.applyPoint(this.lastPoint, index, progress);
                this.applyAngle(angle, index, progress);

                return;
            }

            /* Navigate progress into correct direction */
            if (progress > 1)
            {
                if (index >= size)
                {
                    progress = 1;
                    factor *= -0.5F;
                }
                else
                {
                    progress = progress % 1;
                    index++;
                }
            }
            else if (progress < 0)
            {
                if (index <= 0)
                {
                    progress = 0;
                    factor *= -0.5F;
                }
                else
                {
                    progress = 1 + progress % 1;
                    index--;
                }
            }

            /* Calculate distance and delta from previous iteration */
            this.applyPoint(this.tmpPoint, index, progress);
            double dx = this.tmpPoint.x - this.lastPoint.x;
            double dy = this.tmpPoint.y - this.lastPoint.y;
            double dz = this.tmpPoint.z - this.lastPoint.z;
            this.lastPoint.set(this.tmpPoint.x, this.tmpPoint.y, this.tmpPoint.z);

            distance += Math.sqrt(dx * dx + dy * dy + dz * dz) * (factor > 0 ? 1 : -1);
            float delta = Math.abs(target - distance);

            /* This piece makes sure that if the path's distance is less 
             * than targets, that means that we reached the end of the 
             * path, so there is no point getting closet to the target */
            if (progress == 1 && index >= size && distance < target)
            {
                break;
            }

            /* If last difference is less than new delta between target 
             * distance and path distance, then we're going away from 
             * target, align factor back into target's direction */
            if (diff < delta)
            {
                factor *= -0.5F;
            }

            diff = delta;
        }

        this.applyAngle(angle, index, progress);
    }

    /**
     * Apply point 
     */
    private void applyPoint(Point point, int index, float progress)
    {
        double x = 0, y = 0, z = 0;

        Position p0 = this.getPoint(index - 1);
        Position p1 = this.getPoint(index);
        Position p2 = this.getPoint(index + 1);
        Position p3 = this.getPoint(index + 2);

        /* Interpolating the position */
        InterpolationType interp = this.interpolationPos;

        if (interp == InterpolationType.CUBIC)
        {
            x = Interpolations.cubic(p0.point.x, p1.point.x, p2.point.x, p3.point.x, progress);
            y = Interpolations.cubic(p0.point.y, p1.point.y, p2.point.y, p3.point.y, progress);
            z = Interpolations.cubic(p0.point.z, p1.point.z, p2.point.z, p3.point.z, progress);
        }
        else if (interp == InterpolationType.HERMITE)
        {
            x = Interpolations.cubicHermite(p0.point.x, p1.point.x, p2.point.x, p3.point.x, progress);
            y = Interpolations.cubicHermite(p0.point.y, p1.point.y, p2.point.y, p3.point.y, progress);
            z = Interpolations.cubicHermite(p0.point.z, p1.point.z, p2.point.z, p3.point.z, progress);
        }
        else if (interp.interp != null)
        {
            Interpolation func = interp.function;

            x = (double) func.interpolate((float) p1.point.x, (float) p2.point.x, progress);
            y = (double) func.interpolate((float) p1.point.y, (float) p2.point.y, progress);
            z = (double) func.interpolate((float) p1.point.z, (float) p2.point.z, progress);
        }

        point.set(x, y, z);
    }

    /**
     * Apply angle  
     */
    private void applyAngle(Angle angle, int index, float progress)
    {
        float yaw = 0, pitch = 0, roll = 0, fov = 0;

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
        InterpolationType interp = this.interpolationAngle;

        if (interp == InterpolationType.CUBIC)
        {
            yaw = Interpolations.cubic(p0.angle.yaw, p1.angle.yaw, p2.angle.yaw, p3.angle.yaw, progress);
            pitch = Interpolations.cubic(p0.angle.pitch, p1.angle.pitch, p2.angle.pitch, p3.angle.pitch, progress);
            roll = Interpolations.cubic(p0.angle.roll, p1.angle.roll, p2.angle.roll, p3.angle.roll, progress);
            fov = Interpolations.cubic(p0.angle.fov, p1.angle.fov, p2.angle.fov, p3.angle.fov, progress);
        }
        else if (interp == InterpolationType.HERMITE)
        {
            yaw = (float) Interpolations.cubicHermite(p0.angle.yaw, p1.angle.yaw, p2.angle.yaw, p3.angle.yaw, progress);
            pitch = (float) Interpolations.cubicHermite(p0.angle.pitch, p1.angle.pitch, p2.angle.pitch, p3.angle.pitch, progress);
            roll = (float) Interpolations.cubicHermite(p0.angle.roll, p1.angle.roll, p2.angle.roll, p3.angle.roll, progress);
            fov = (float) Interpolations.cubicHermite(p0.angle.fov, p1.angle.fov, p2.angle.fov, p3.angle.fov, progress);
        }
        else if (interp.interp != null)
        {
            Interpolation func = interp.function;

            yaw = func.interpolate(p1.angle.yaw, p2.angle.yaw, progress);
            pitch = func.interpolate(p1.angle.pitch, p2.angle.pitch, progress);
            roll = func.interpolate(p1.angle.roll, p2.angle.roll, progress);
            fov = func.interpolate(p1.angle.fov, p2.angle.fov, progress);
        }

        angle.set(yaw, pitch, roll, fov);
    }

    /* Save/load methods */

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

        this.useSpeed = buffer.readBoolean();
        this.speed.fromByteBuf(buffer);
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        buffer.writeBoolean(this.perPointDuration);
        buffer.writeByte(this.interpolationPos.ordinal());
        buffer.writeByte(this.interpolationAngle.ordinal());

        buffer.writeInt(this.points.size());

        for (DurablePosition pos : this.points)
        {
            pos.toByteBuf(buffer);
        }

        buffer.writeBoolean(this.useSpeed);
        this.speed.toByteBuf(buffer);
    }

    @Override
    public AbstractFixture clone()
    {
        PathFixture fixture = new PathFixture(this.duration);

        AbstractFixture.copyModifiers(this, fixture);
        for (DurablePosition pos : this.points)
        {
            fixture.addPoint((DurablePosition) pos.clone());
        }

        fixture.name = this.name;
        fixture.perPointDuration = this.perPointDuration;
        fixture.interpolationPos = this.interpolationPos;
        fixture.interpolationAngle = this.interpolationAngle;

        fixture.useSpeed = this.useSpeed;
        fixture.speed.copy(this.speed);

        return fixture;
    }

    /* Interpolation */

    public static InterpolationType interpFromInt(int number)
    {
        if (number < 0 || number >= InterpolationType.values().length)
        {
            return InterpolationType.LINEAR;
        }

        return InterpolationType.values()[number];
    }

    public static InterpolationType interpFromString(String string)
    {
        for (InterpolationType type : InterpolationType.values())
        {
            if (type.name.equals(string))
            {
                return type;
            }
        }

        return InterpolationType.LINEAR;
    }

    public static enum InterpolationType
    {
        LINEAR(Interpolation.LINEAR, KeyframeInterpolation.LINEAR), CUBIC("cubic", KeyframeInterpolation.CUBIC), HERMITE("hermite", KeyframeInterpolation.HERMITE),
        /* Quadratic interpolations */
        QUAD_IN(Interpolation.QUAD_IN, KeyframeInterpolation.QUAD, Easing.IN), QUAD_OUT(Interpolation.QUAD_OUT, KeyframeInterpolation.QUAD, Easing.OUT), QUAD_INOUT(Interpolation.QUAD_INOUT, KeyframeInterpolation.QUAD, Easing.INOUT),
        /* Cubic interpolations */
        CUBIC_IN(Interpolation.CUBIC_IN, KeyframeInterpolation.CUBIC, Easing.IN), CUBIC_OUT(Interpolation.CUBIC_OUT, KeyframeInterpolation.CUBIC, Easing.OUT), CUBIC_INOUT(Interpolation.CUBIC_INOUT, KeyframeInterpolation.CUBIC, Easing.INOUT),
        /* Exponential interpolations */
        EXP_IN(Interpolation.EXP_IN, KeyframeInterpolation.EXP, Easing.IN), EXP_OUT(Interpolation.EXP_OUT, KeyframeInterpolation.EXP, Easing.OUT), EXP_INOUT(Interpolation.EXP_INOUT, KeyframeInterpolation.EXP, Easing.INOUT);

        public final String name;
        public Interpolation function;
        public KeyframeInterpolation interp;
        public Easing easing = Easing.IN;

        private InterpolationType(String name)
        {
            this.name = name;
        }

        private InterpolationType(String name, KeyframeInterpolation interp)
        {
            this(name, interp, Easing.IN);
        }

        private InterpolationType(String name, KeyframeInterpolation interp, Easing easing)
        {
            this.name = name;
            this.interp = interp;
            this.easing = easing;
        }

        private InterpolationType(Interpolation function)
        {
            this.name = function.key;
            this.function = function;
        }

        private InterpolationType(Interpolation function, KeyframeInterpolation interp)
        {
            this(function, interp, Easing.IN);
        }

        private InterpolationType(Interpolation function, KeyframeInterpolation interp, Easing easing)
        {
            this.name = function.key;
            this.function = function;
            this.interp = interp;
            this.easing = easing;
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

        public DurablePosition(Position pos)
        {
            this(pos.point, pos.angle);
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
        public Position clone()
        {
            return new DurablePosition(this.duration, this.point.clone(), this.angle.clone());
        }

        @Override
        public void toByteBuf(ByteBuf buffer)
        {
            buffer.writeLong(this.duration);

            super.toByteBuf(buffer);
        }
    }
}