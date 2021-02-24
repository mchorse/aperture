package mchorse.aperture.camera.fixtures;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.values.ValueInterpolationType;
import mchorse.aperture.camera.values.ValueKeyframeChannel;
import mchorse.aperture.camera.values.ValuePositions;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueDouble;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import mchorse.mclib.utils.keyframes.KeyframeEasing;
import mchorse.mclib.utils.keyframes.KeyframeInterpolation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;

/**
 * Path camera fixture
 *
 * This fixture is responsible for making smooth camera movements.
 */
public class PathFixture extends AbstractFixture
{
    public static final Vector2d VECTOR = new Vector2d();

    /**
     * List of points in this fixture
     */
    public final ValuePositions points = new ValuePositions("points");

    public final ValueInterpolationType interpolation = new ValueInterpolationType("interpolation");
    public final ValueInterpolationType interpolationAngle = new ValueInterpolationType("interpolationAngle");

    /**
     * Whether keyframe-able speed should be used for this
     */
    public final ValueBoolean useSpeed = new ValueBoolean("useSpeed", false);

    /**
     * Keyframe-able speed
     */
    public final ValueKeyframeChannel speed = new ValueKeyframeChannel("speed");

    public final ValueBoolean circularAutoCenter = new ValueBoolean("circularAutoCenter", true);
    public final ValueDouble circularX = new ValueDouble("circularX", 0);
    public final ValueDouble circularZ = new ValueDouble("circularZ",0);

    /* Speed related cache data */
    private float lastTick;
    private Point lastPoint = new Point(0, 0, 0);
    private Point tmpPoint = new Point(0, 0, 0);
    private CachedPosition result = new CachedPosition();
    private List<CachedPosition> cache = new ArrayList<CachedPosition>();
    private boolean disableSpeed = false;

    public PathFixture(long duration)
    {
        super(duration);

        this.register(this.points);
        this.register(this.interpolation);
        this.register(this.interpolationAngle);

        this.register(this.useSpeed);
        this.register(this.speed);

        this.register(this.circularAutoCenter);
        this.register(this.circularX);
        this.register(this.circularZ);

        this.speed.get().insert(0, 1);
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
        KeyframeInterpolation pos = this.interpolation.get().interp;
        KeyframeInterpolation angle = this.interpolationAngle.get().interp;
        KeyframeEasing posEasing = this.interpolation.get().easing;
        KeyframeEasing angleEasing = this.interpolationAngle.get().easing;

        long x;
        int i = 0;

        for (Position point : this.getPoints())
        {
            x = (int) (i / (c - 1F) * duration);

            int index = fixture.x.get().insert(x, (float) point.point.x);
            fixture.y.get().insert(x, (float) point.point.y);
            fixture.z.get().insert(x, (float) point.point.z);
            fixture.yaw.get().insert(x, point.angle.yaw);
            fixture.pitch.get().insert(x, point.angle.pitch);
            fixture.roll.get().insert(x, point.angle.roll);
            fixture.fov.get().insert(x, point.angle.fov);

            fixture.x.get().get(index).setInterpolation(pos, posEasing);
            fixture.y.get().get(index).setInterpolation(pos, posEasing);
            fixture.z.get().get(index).setInterpolation(pos, posEasing);
            fixture.yaw.get().get(index).setInterpolation(angle, angleEasing);
            fixture.pitch.get().get(index).setInterpolation(angle, angleEasing);
            fixture.roll.get().get(index).setInterpolation(angle, angleEasing);
            fixture.fov.get().get(index).setInterpolation(angle, angleEasing);

            i ++;
        }

        return fixture;
    }

    @Override
    public void initiate()
    {
        this.updateSpeedCache();
    }

    public void updateSpeedCache()
    {
        CachedPosition previous = null;

        this.cache.clear();

        for (int i = 1, c = this.getCount(); i < c; i ++)
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
        int size = this.getCount();

        if (size == 0)
        {
            return new Position(0, 0, 0, 0, 0);
        }

        if (index >= size)
        {
            return this.points.get().get(size - 1);
        }

        if (index < 0)
        {
            return this.points.get().get(0);
        }

        return this.points.get().get(index);
    }

    public List<Position> getPoints()
    {
        return this.points.get();
    }

    public int getCount()
    {
        return this.getCount();
    }

    /**
     * Return the frame for point at the index   
     */
    public long getTickForPoint(int index)
    {
        return (long) ((index / (float) (this.getCount() - 1)) * this.getDuration());
    }

    @Override
    public void fromPlayer(EntityPlayer player)
    {
        this.points.get().add(new Position(player));
    }

    @Override
    public void applyFixture(long ticks, float partialTicks, float previewPartialTick, CameraProfile profile, Position pos)
    {
        long duration = this.getDuration();

        if (this.points.get().isEmpty() || duration == 0)
        {
            return;
        }

        /* If use speed is enabled */
        if (this.useSpeed.get() && !this.disableSpeed)
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
            int length = this.getCount() - 1;
            int index;
            float x;

            x = (ticks / (float) this.getDuration()) + (1.0F / duration) * previewPartialTick;
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
        int size = this.getCount() - 1;
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

        KeyframeChannel channel = this.speed.get();

        for (int i = 0, c = (int) tick; i < c; i++)
        {
            target += channel.interpolate(i);
        }

        target += channel.interpolate(tick) * (tick % 1);
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
        InterpolationType interp = this.interpolation.get();

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
        else if (interp == InterpolationType.CIRCULAR)
        {
            int size = this.getCount();

            if (index >= size)
            {
                x = p2.point.x;
                y = p2.point.y;
                z = p2.point.z;
            }
            else if (index < 0)
            {
                x = p1.point.x;
                y = p1.point.y;
                z = p1.point.z;
            }
            else
            {
                Vector2d center = this.getCenter();

                double mx = center.x;
                double mz = center.y;

                Vector2d a0 = this.calculateCircular(mx, mz, index - 1);
                Vector2d a1 = this.calculateCircular(mx, mz, index);
                Vector2d a2 = this.calculateCircular(mx, mz, index + 1);
                Vector2d a3 = this.calculateCircular(mx, mz, index + 2);

                double a = Interpolations.cubicHermite(a0.x, a1.x, a2.x, a3.x, progress);
                double d = Interpolations.cubicHermite(a0.y, a1.y, a2.y, a3.y, progress);

                a = a / 180 * Math.PI;

                x = mx + Math.cos(a) * d;
                y = Interpolations.cubicHermite(p0.point.y, p1.point.y, p2.point.y, p3.point.y, progress);
                z = mz + Math.sin(a) * d;
            }
        }
        else if (interp.interp != null)
        {
            Interpolation func = interp.function;

            x = func.interpolate(p1.point.x, p2.point.x, progress);
            y = func.interpolate(p1.point.y, p2.point.y, progress);
            z = func.interpolate(p1.point.z, p2.point.z, progress);
        }

        point.set(x, y, z);
    }

    private Vector2d calculateCircular(double mx, double mz, int index)
    {
        int size = this.getCount();

        double a = 0;
        double d = 0;
        double lastA = 0;

        if (index < 0)
        {
            index = 0;
        }
        else if (index >= size)
        {
            index = size - 1;
        }

        for (int i = 0; i < size; i++)
        {
            Position p = this.points.get().get(i);

            double dx = p.point.x - mx;
            double dz = p.point.z - mz;

            d = Math.sqrt(dx * dx + dz * dz);
            a = Math.atan2(dz, dx) / Math.PI * 180;

            if (a < 0)
            {
                a = 360 + a;
            }

            double originalA = a;

            if (Math.abs(a - lastA) > 180)
            {
                a = Interpolations.normalizeYaw(lastA, a);
            }

            if (i == index)
            {
                break;
            }

            lastA = originalA;
        }

        return new Vector2d(a, d);
    }

    public Vector2d getCenter()
    {
        if (this.circularAutoCenter.get())
        {
            this.calculateCenter(VECTOR);
        }
        else
        {
            VECTOR.set(this.circularX.get(), this.circularZ.get());
        }

        return VECTOR;
    }

    public Vector2d calculateCenter(Vector2d vector)
    {
        vector.set(0, 0);

        for (Position position : this.points.get())
        {
            vector.x += position.point.x;
            vector.y += position.point.z;
        }

        vector.x /= this.getCount();
        vector.y /= this.getCount();

        return vector;
    }

    /**
     * Apply angle  
     */
    private void applyAngle(Angle angle, int index, float progress)
    {
        float yaw = 0, pitch = 0, roll = 0, fov = 0;

        Position p0 = this.getPoint(index - 1);
        Position p1 = this.getPoint(index);
        Position p2 = this.getPoint(index + 1);
        Position p3 = this.getPoint(index + 2);

        /* Interpolating the angle */
        InterpolationType interp = this.interpolationAngle.get();

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
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.points.fromBytes(buffer);
        this.interpolation.fromBytes(buffer);
        this.interpolationAngle.fromBytes(buffer);

        this.useSpeed.fromBytes(buffer);
        this.speed.fromBytes(buffer);

        this.circularAutoCenter.fromBytes(buffer);
        this.circularX.fromBytes(buffer);
        this.circularZ.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.points.toBytes(buffer);
        this.interpolation.toBytes(buffer);
        this.interpolationAngle.toBytes(buffer);

        this.useSpeed.toBytes(buffer);
        this.speed.toBytes(buffer);

        this.circularAutoCenter.toBytes(buffer);
        this.circularX.toBytes(buffer);
        this.circularZ.toBytes(buffer);
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

            this.points.copy(path.points);
            this.interpolation.copy(path.interpolation);
            this.interpolationAngle.copy(path.interpolationAngle);

            this.useSpeed.copy(path.useSpeed);
            this.speed.copy(path.speed);

            this.circularAutoCenter.copy(path.circularAutoCenter);
            this.circularX.copy(path.circularX);
            this.circularZ.copy(path.circularZ);
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

            this.points.reset();
            this.points.get().add(dolly.position.get().copy());
            this.points.get().add(position);
            this.interpolation.set(InterpolationType.fromInterp(dolly.interp.get()));
            this.interpolationAngle.set(InterpolationType.fromInterp(dolly.interp.get()));
        }
    }

    /* Interpolation */

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
        BOUNCE_IN(Interpolation.BOUNCE_IN, KeyframeInterpolation.BOUNCE, KeyframeEasing.IN), BOUNCE_OUT(Interpolation.BOUNCE_OUT, KeyframeInterpolation.BOUNCE, KeyframeEasing.OUT), BOUNCE_INOUT(Interpolation.BOUNCE_INOUT, KeyframeInterpolation.BOUNCE, KeyframeEasing.INOUT),
        /* Exclusive (no way to convert properly to keyframe) */
        CIRCULAR("circular", KeyframeInterpolation.LINEAR);

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