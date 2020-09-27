package mchorse.aperture.camera.fixtures;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MathUtils;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import mchorse.mclib.utils.keyframes.KeyframeEasing;
import mchorse.mclib.utils.keyframes.KeyframeInterpolation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Path camera fixture
 *
 * This fixture is responsible for making smooth camera movements.
 */
public class PathFixture extends AbstractFixture
{
    /**
     * List of points in this fixture 
     */
    @Expose
    protected List<Position> points = new ArrayList<Position>();

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
    private CachedPosition result = new CachedPosition();
    private List<CachedPosition> cache = new ArrayList<CachedPosition>();
    private boolean disableSpeed = false;

    public PathFixture()
    {
        super(0);

        this.speed = new KeyframeChannel();
    }

    public PathFixture(long duration)
    {
        super(duration);

        InterpolationType type = InterpolationType.HERMITE;

        this.interpolationPos = type;
        this.interpolationAngle = type;
        this.speed = new KeyframeChannel();
        this.speed.insert(0, 1);
    }

    public KeyframeFixture toKeyframe()
    {
        int c = this.getCount();

        if (c <= 1)
        {
            return null;
        }

        long duration = this.getDuration();
        KeyframeFixture fixture = new KeyframeFixture(duration);

        fixture.copy(this);
        KeyframeInterpolation pos = this.interpolationPos.interp;
        KeyframeInterpolation angle = this.interpolationAngle.interp;
        KeyframeEasing posEasing = this.interpolationPos.easing;
        KeyframeEasing angleEasing = this.interpolationAngle.easing;

        long x;
        int i = 0;

        for (Position point : this.getPoints())
        {
            x = (int) (i / (c - 1F) * duration);

            int index = fixture.x.insert(x, (float) point.point.x);
            fixture.y.insert(x, (float) point.point.y);
            fixture.z.insert(x, (float) point.point.z);
            fixture.yaw.insert(x, point.angle.yaw);
            fixture.pitch.insert(x, point.angle.pitch);
            fixture.roll.insert(x, point.angle.roll);
            fixture.fov.insert(x, point.angle.fov);

            fixture.x.get(index).setInterpolation(pos, posEasing);
            fixture.y.get(index).setInterpolation(pos, posEasing);
            fixture.z.get(index).setInterpolation(pos, posEasing);
            fixture.yaw.get(index).setInterpolation(angle, angleEasing);
            fixture.pitch.get(index).setInterpolation(angle, angleEasing);
            fixture.roll.get(index).setInterpolation(angle, angleEasing);
            fixture.fov.get(index).setInterpolation(angle, angleEasing);

            i ++;
        }

        return fixture;
    }

    @Override
    public void initiate()
    {
        this.speed.sort();
        this.updateSpeedCache();
    }

    public void updateSpeedCache()
    {
        CachedPosition previous = null;

        this.cache.clear();

        for (int i = 1, c = this.points.size(); i < c; i ++)
        {
            float target = this.calculateTarget((int) (this.getDuration() / (float) c * i));

            this.applyPoint(this.lastPoint, 0, 0);
            this.cache.add(previous = this.calculateResult(target, true, previous).copy());
        }
    }

    public void disableSpeed()
    {
        this.disableSpeed = true;
    }

    public void reenableSpeed()
    {
        this.disableSpeed = false;
    }

    public Position getPoint(int index)
    {
        if (this.points.size() == 0)
        {
            return new Position(0, 0, 0, 0, 0);
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

    public List<Position> getPoints()
    {
        return this.points;
    }

    public int getCount()
    {
        return this.points.size();
    }

    public void addPoint(Position point)
    {
        this.points.add(point);
    }

    public void addPoint(Position point, int before)
    {
        this.points.add(before, point);
    }

    public void movePoint(int from, int to)
    {
        this.points.add(to, this.points.remove(from));
    }

    public void editPoint(Position point, int index)
    {
        this.points.set(index, point);
    }

    public void removePoint(int index)
    {
        this.points.remove(index);
    }

    /**
     * Return index of a point at given frame (relative to that path fixture, i.e. 0)  
     */
    public int getIndexForPoint(int frame)
    {
        float range = (float) frame / this.duration;
        int index = (int) Math.floor(range * (this.points.size() - 1));

        return MathHelper.clamp(index, 0, (int) this.duration);
    }

    /**
     * Return the frame for point at the index   
     */
    public long getTickForPoint(int index)
    {
        return (long) ((index / (float) (this.points.size() - 1)) * this.duration);
    }

    @Override
    public void fromPlayer(EntityPlayer player)
    {
        this.addPoint(new Position(player));
    }

    @Override
    public void applyFixture(long ticks, float partialTicks, float previewPartialTick, CameraProfile profile, Position pos)
    {
        long duration = this.getDuration();

        if (this.points.isEmpty() || duration == 0)
        {
            return;
        }

        /* If use speed is enabled */
        if (this.useSpeed && !this.disableSpeed)
        {
            float tick = ticks + previewPartialTick;

            /* Just calculate enough for the speed for the difference */
            if (tick != this.lastTick || tick == 0)
            {
                this.applyPoint(this.lastPoint, 0, 0);
                this.recalculate(tick, pos.angle);
            }

            pos.point.set(this.lastPoint.x, this.lastPoint.y, this.lastPoint.z);
            this.lastTick = tick;
        }
        else
        {
            int length = this.points.size() - 1;
            int index;
            float x;

            x = (ticks / (float) this.duration) + (1.0F / duration) * previewPartialTick;
            x = MathHelper.clamp    (x * length, 0, length);
            index = (int) Math.floor(x);
            x = x - index;

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
    private CachedPosition recalculate(float tick, Angle angle)
    {
        float target = this.calculateTarget(tick);
        CachedPosition result = null;

        for (int i = 1, c = this.cache.size(); i < c; i ++)
        {
            CachedPosition current = this.cache.get(i);
            CachedPosition previous = this.cache.get(i - 1);

            if (target < current.distance && target >= previous.distance)
            {
                result = previous;

                break;
            }

            if (i == c - 1 && target >= current.distance)
            {
                result = current;
            }
        }

        result = this.calculateResult(target, result);
        this.applyAngle(angle, result.index, result.progress);

        return result;
    }

    private CachedPosition calculateResult(float target)
    {
        return this.calculateResult(target, null);
    }

    private CachedPosition calculateResult(float target, CachedPosition position)
    {
        return this.calculateResult(target, false, position);
    }

    private CachedPosition calculateResult(float target, boolean haltOnFactorChange, CachedPosition position)
    {
        /* Try to calculate the actual distance traveled.
         *
         * The loop below doesn't yield *exact* position based on ticks
         * but rather an approximation. It starts from beginning, and
         * tries to calculate the point that is close enough to the
         * target */
        int iterations = 0;
        int size = this.points.size() - 1;
        int index = 0;
        float progress = 0;
        float distance = 0;
        float factor = 0.1F;

        if (position != null)
        {
            index = position.index;
            progress = position.progress;
            distance = position.distance;
            this.lastPoint.set(position.point);
        }

        float diff = Math.abs(target - distance);

        while (diff > 0.00005F)
        {
            progress += factor;

            /* To avoid infinite loop, we break things here. Factor with
             * every iteration is definitely getting smaller */
            if (factor == 0 || Math.abs(factor) < 0.0000001F)
            {
                this.result.set(index, progress, distance, iterations, this.lastPoint);
                this.applyPoint(this.lastPoint, index, progress);

                return this.result;
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
             * path, so there is no point getting closer to the target */
            if (progress == 1 && index >= size && distance < target)
            {
                break;
            }

            /* If last difference is less than new delta between target
             * distance and path distance, then we're going away from
             * target, align factor back into target's direction */
            if (diff < delta)
            {
                if (haltOnFactorChange)
                {
                    this.result.set(index, progress, distance, iterations, this.lastPoint);

                    return this.result;
                }

                factor *= -0.5F;
            }

            diff = delta;
            iterations ++;
        }

        this.result.set(index, progress, distance, iterations, this.lastPoint);

        return this.result;
    }

    /**
     * Calculate target distance from the first point that the path
     * should be at given tick (+partialTick)
     */
    private float calculateTarget(float tick)
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

        return target;
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
        super.toJSON(object);

        object.addProperty("interpolation", this.interpolationPos.name);
        object.addProperty("interpolationAngle", this.interpolationAngle.name);
    }

    @Override
    public void fromJSON(JsonObject object)
    {
        super.fromJSON(object);

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

        this.interpolationPos = interpFromInt(buffer.readByte());
        this.interpolationAngle = interpFromInt(buffer.readByte());

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            this.addPoint(Position.fromByteBuf(buffer));
        }

        this.useSpeed = buffer.readBoolean();
        this.speed.fromByteBuf(buffer);
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        buffer.writeByte(this.interpolationPos.ordinal());
        buffer.writeByte(this.interpolationAngle.ordinal());

        buffer.writeInt(this.points.size());

        for (Position pos : this.points)
        {
            pos.toByteBuf(buffer);
        }

        buffer.writeBoolean(this.useSpeed);
        this.speed.toByteBuf(buffer);
    }

    @Override
    public AbstractFixture create(long duration)
    {
        return new PathFixture(duration);
    }

    @Override
    public void copy(AbstractFixture from)
    {
        super.copy(from);

        if (from instanceof PathFixture)
        {
            PathFixture path = (PathFixture) from;

            for (Position pos : path.points)
            {
                this.addPoint(pos.copy());
            }

            this.interpolationPos = path.interpolationPos;
            this.interpolationAngle = path.interpolationAngle;

            this.useSpeed = path.useSpeed;
            this.speed.copy(path.speed);
        }
    }

    @Override
    public void copyByReplacing(AbstractFixture from)
    {
        super.copyByReplacing(from);

        if (from instanceof DollyFixture)
        {
            DollyFixture dolly = (DollyFixture) from;
            Position position = new Position();

            from.applyLast(null, position);

            this.points.clear();
            this.points.add(dolly.position.copy());
            this.points.add(position);
            this.interpolationPos = this.interpolationAngle = InterpolationType.fromInterp(dolly.interp);
        }
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
        QUAD_IN(Interpolation.QUAD_IN, KeyframeInterpolation.QUAD, KeyframeEasing.IN), QUAD_OUT(Interpolation.QUAD_OUT, KeyframeInterpolation.QUAD, KeyframeEasing.OUT), QUAD_INOUT(Interpolation.QUAD_INOUT, KeyframeInterpolation.QUAD, KeyframeEasing.INOUT),
        /* Cubic interpolations */
        CUBIC_IN(Interpolation.CUBIC_IN, KeyframeInterpolation.CUBIC, KeyframeEasing.IN), CUBIC_OUT(Interpolation.CUBIC_OUT, KeyframeInterpolation.CUBIC, KeyframeEasing.OUT), CUBIC_INOUT(Interpolation.CUBIC_INOUT, KeyframeInterpolation.CUBIC, KeyframeEasing.INOUT),
        /* Exponential interpolations */
        EXP_IN(Interpolation.EXP_IN, KeyframeInterpolation.EXP, KeyframeEasing.IN), EXP_OUT(Interpolation.EXP_OUT, KeyframeInterpolation.EXP, KeyframeEasing.OUT), EXP_INOUT(Interpolation.EXP_INOUT, KeyframeInterpolation.EXP, KeyframeEasing.INOUT),
        /* Back interpolations */
        BACK_IN(Interpolation.BACK_IN, KeyframeInterpolation.BACK, KeyframeEasing.IN), BACK_OUT(Interpolation.BACK_OUT, KeyframeInterpolation.BACK, KeyframeEasing.OUT), BACK_INOUT(Interpolation.BACK_INOUT, KeyframeInterpolation.BACK, KeyframeEasing.INOUT),
        /* Back interpolations */
        ELASTIC_IN(Interpolation.ELASTIC_IN, KeyframeInterpolation.ELASTIC, KeyframeEasing.IN), ELASTIC_OUT(Interpolation.ELASTIC_OUT, KeyframeInterpolation.ELASTIC, KeyframeEasing.OUT), ELASTIC_INOUT(Interpolation.ELASTIC_INOUT, KeyframeInterpolation.ELASTIC, KeyframeEasing.INOUT),
        /* Back interpolations */
        BOUNCE_IN(Interpolation.BOUNCE_IN, KeyframeInterpolation.BOUNCE, KeyframeEasing.IN), BOUNCE_OUT(Interpolation.BOUNCE_OUT, KeyframeInterpolation.BOUNCE, KeyframeEasing.OUT), BOUNCE_INOUT(Interpolation.BOUNCE_INOUT, KeyframeInterpolation.BOUNCE, KeyframeEasing.INOUT);

        public final String name;
        public Interpolation function;
        public KeyframeInterpolation interp;
        public KeyframeEasing easing = KeyframeEasing.IN;

        private InterpolationType(String name)
        {
            this.name = name;
        }

        private InterpolationType(String name, KeyframeInterpolation interp)
        {
            this(name, interp, KeyframeEasing.IN);
        }

        private InterpolationType(String name, KeyframeInterpolation interp, KeyframeEasing easing)
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
            this(function, interp, KeyframeEasing.IN);
        }

        private InterpolationType(Interpolation function, KeyframeInterpolation interp, KeyframeEasing easing)
        {
            this.name = function.key;
            this.function = function;
            this.interp = interp;
            this.easing = easing;
        }

        public static InterpolationType fromInterp(Interpolation interp)
        {
            for (InterpolationType type : values())
            {
                if (type.function == interp)
                {
                    return type;
                }
            }

            return LINEAR;
        }

        public String getKey()
        {
            return "mclib.interpolations." + this.name;
	    }
    }

    public static class CachedPosition
    {
        public int index;
        public float progress;
        public float distance;
        public int iterations;
        public Point point;

        public CachedPosition()
        {}

        public void set(int index, float progress, float distance, int iterations, Point point)
        {
            this.index = index;
            this.progress = progress;
            this.distance = distance;
            this.iterations = iterations;
            this.point = point.copy();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof CachedPosition)
            {
                CachedPosition position = (CachedPosition) obj;

                return this.index == position.index && this.progress == position.progress && this.distance == position.distance
                    && this.point.x == position.point.x && this.point.y == position.point.y && this.point.z == position.point.z;
            }

            return super.equals(obj);
        }

        public CachedPosition copy()
        {
            CachedPosition position = new CachedPosition();

            position.set(this.index, this.progress, this.distance, this.iterations, this.point);

            return position;
        }
    }
}