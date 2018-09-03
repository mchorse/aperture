package mchorse.aperture.camera.fixtures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.smooth.Interpolations;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Keyframe fixture
 * 
 * This fixture provides a much flexible control over camera, allowing setting 
 * up different transitions between points with different easing.
 */
public class KeyframeFixture extends AbstractFixture
{
    /* Different animatable channels */

    @Expose
    public final KeyframeChannel x = new KeyframeChannel();

    @Expose
    public final KeyframeChannel y = new KeyframeChannel();

    @Expose
    public final KeyframeChannel z = new KeyframeChannel();

    @Expose
    public final KeyframeChannel yaw = new KeyframeChannel();

    @Expose
    public final KeyframeChannel pitch = new KeyframeChannel();

    @Expose
    public final KeyframeChannel roll = new KeyframeChannel();

    @Expose
    public final KeyframeChannel fov = new KeyframeChannel();

    public KeyframeFixture(long duration)
    {
        super(duration);
    }

    @Override
    public void edit(String[] args, EntityPlayer player) throws CommandException
    {}

    @Override
    public void applyFixture(long ticks, float partialTick, Position pos)
    {
        float t = ticks + partialTick;

        if (!this.x.isEmpty()) pos.point.x = this.x.interpolate(t);
        if (!this.y.isEmpty()) pos.point.y = this.y.interpolate(t);
        if (!this.z.isEmpty()) pos.point.z = this.z.interpolate(t);
        if (!this.yaw.isEmpty()) pos.angle.yaw = this.yaw.interpolate(t);
        if (!this.pitch.isEmpty()) pos.angle.pitch = this.pitch.interpolate(t);
        if (!this.roll.isEmpty()) pos.angle.roll = this.roll.interpolate(t);
        if (!this.fov.isEmpty()) pos.angle.fov = this.fov.interpolate(t);
    }

    @Override
    public AbstractFixture clone()
    {
        KeyframeFixture fixture = new KeyframeFixture(this.duration);

        AbstractFixture.copyModifiers(this, fixture);
        fixture.x.copy(this.x);
        fixture.y.copy(this.y);
        fixture.z.copy(this.z);
        fixture.yaw.copy(this.yaw);
        fixture.pitch.copy(this.pitch);
        fixture.roll.copy(this.roll);
        fixture.fov.copy(this.fov);

        return fixture;
    }

    @Override
    public void fromJSON(JsonObject object)
    {
        this.x.sort();
        this.y.sort();
        this.z.sort();
        this.yaw.sort();
        this.pitch.sort();
        this.roll.sort();
        this.fov.sort();
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.x.fromByteBuf(buffer);
        this.y.fromByteBuf(buffer);
        this.z.fromByteBuf(buffer);
        this.yaw.fromByteBuf(buffer);
        this.pitch.fromByteBuf(buffer);
        this.roll.fromByteBuf(buffer);
        this.fov.fromByteBuf(buffer);
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        this.x.toByteBuf(buffer);
        this.y.toByteBuf(buffer);
        this.z.toByteBuf(buffer);
        this.yaw.toByteBuf(buffer);
        this.pitch.toByteBuf(buffer);
        this.roll.toByteBuf(buffer);
        this.fov.toByteBuf(buffer);
    }

    /**
     * Keyframe channel
     * 
     * This class is responsible for storing individual keyframes and also 
     * interpolating between them.
     */
    public static class KeyframeChannel
    {
        @Expose
        public final List<Keyframe> keyframes = new ArrayList<Keyframe>();

        public boolean isEmpty()
        {
            return this.keyframes.isEmpty();
        }

        /**
         * Calculate the value at given tick 
         */
        public float interpolate(float ticks)
        {
            Keyframe prev = this.keyframes.get(0);

            if (ticks < prev.tick)
            {
                return prev.value;
            }

            prev = null;

            for (Keyframe frame : this.keyframes)
            {
                if (prev != null && ticks >= prev.tick && ticks < frame.tick)
                {
                    return prev.interpolate(frame, (ticks - prev.tick) / (frame.tick - prev.tick));
                }

                prev = frame;
            }

            return prev.value;
        }

        /**
         * Insert a keyframe at given tick with given value
         * 
         * This method is useful as it's not creating keyframes every time you 
         * need to add some value, but rather inserts in correct order or 
         * overwrites existing keyframe.
         * 
         * Also it returns index at which it was inserted.
         */
        public int insert(long tick, float value)
        {
            Keyframe prev = this.keyframes.get(0);

            if (tick < prev.tick)
            {
                this.keyframes.add(0, new Keyframe(tick, value));

                return 0;
            }

            prev = null;
            int index = 0;

            for (Keyframe frame : this.keyframes)
            {
                if (frame.tick == tick)
                {
                    frame.value = value;

                    return index;
                }

                if (prev != null && tick > prev.tick && tick < frame.tick)
                {
                    break;
                }

                index++;
                prev = frame;
            }

            this.keyframes.add(index, new Keyframe(tick, value));

            return index;
        }

        /**
         * Sorts keyframes based on their ticks. This method should be used 
         * when you modify individual tick values of keyframes. 
         * {@link #interpolate(float)} and other methods assume the order of 
         * the keyframes to be chronologically correct.
         */
        public void sort()
        {
            Collections.sort(this.keyframes, new Comparator<Keyframe>()
            {
                @Override
                public int compare(Keyframe a, Keyframe b)
                {
                    return (int) (a.tick - b.tick);
                }
            });
        }

        public void copy(KeyframeChannel channel)
        {
            this.keyframes.clear();

            for (Keyframe frame : channel.keyframes)
            {
                this.keyframes.add(frame.clone());
            }
        }

        public void fromByteBuf(ByteBuf buffer)
        {
            this.keyframes.clear();

            for (int i = 0, c = buffer.readInt(); i < c; i++)
            {
                Keyframe frame = new Keyframe(buffer.readLong(), buffer.readFloat());

                frame.fromByteBuf(buffer);
                this.keyframes.add(frame);
            }
        }

        public void toByteBuf(ByteBuf buffer)
        {
            buffer.writeInt(this.keyframes.size());

            for (Keyframe frame : this.keyframes)
            {
                frame.toByteBuf(buffer);
            }
        }
    }

    /**
     * Keyframe class
     * 
     * This class is responsible for storing individual keyframe properties such 
     * as tick at which its located, value, interpolation, easing type, etc.
     */
    public static class Keyframe
    {
        @Expose
        public long tick;

        @Expose
        public float value;

        @Expose
        public Interpolation interp = Interpolation.LINEAR;

        @Expose
        public Easing easing = Easing.IN;

        @Expose
        public float rx = 5;

        @Expose
        public float ry;

        @Expose
        public float lx = 5;

        @Expose
        public float ly;

        public Keyframe(long tick, float value)
        {
            this.tick = tick;
            this.value = value;
        }

        public float interpolate(Keyframe frame, float x)
        {
            return this.interp.interpolate(this, frame, x);
        }

        public Keyframe clone()
        {
            Keyframe frame = new Keyframe(this.tick, this.value);

            frame.interp = this.interp;
            frame.easing = this.easing;

            return frame;
        }

        public void fromByteBuf(ByteBuf buffer)
        {
            this.interp = Interpolation.values()[buffer.readInt()];
            this.easing = Easing.values()[buffer.readInt()];
            this.rx = buffer.readFloat();
            this.ry = buffer.readFloat();
            this.lx = buffer.readFloat();
            this.ly = buffer.readFloat();
        }

        public void toByteBuf(ByteBuf buffer)
        {
            buffer.writeLong(this.tick);
            buffer.writeFloat(this.value);
            buffer.writeInt(this.interp.ordinal());
            buffer.writeInt(this.easing.ordinal());
            buffer.writeFloat(this.rx);
            buffer.writeFloat(this.ry);
            buffer.writeFloat(this.lx);
            buffer.writeFloat(this.ly);
        }
    }

    public static enum Interpolation
    {
        CONST
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                return a.value;
            }
        },
        LINEAR
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                return Interpolations.lerp(a.value, b.value, x);
            }
        },
        QUAD
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                if (a.easing == Easing.IN) return a.value + (b.value - a.value) * x * x;
                if (a.easing == Easing.OUT) return a.value - (b.value - a.value) * x * (x - 2);

                x *= 2;

                if (x < 1F) return a.value + (b.value - a.value) / 2 * x * x;

                x -= 1;

                return a.value - (b.value - a.value) / 2 * (x * (x - 2) - 1);
            }
        },
        CUBIC
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                if (a.easing == Easing.IN) return a.value + (b.value - a.value) * x * x * x;
                if (a.easing == Easing.OUT)
                {
                    x -= 1;
                    return a.value + (b.value - a.value) * (x * x * x + 1);
                }

                x *= 2;

                if (x < 1F) return a.value + (b.value - a.value) / 2 * x * x * x;

                x -= 2;

                return a.value + (b.value - a.value) / 2 * (x * x * x + 2);
            }
        },
        EXP
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                if (a.easing == Easing.IN) return a.value + (b.value - a.value) * (float) Math.pow(2, 10 * (x - 1));
                if (a.easing == Easing.OUT) return a.value + (b.value - a.value) * (float) (-Math.pow(2, -10 * x) + 1);

                if (x == 0) return a.value;
                if (x == 1) return b.value;

                x *= 2;

                if (x < 1F) return a.value + (b.value - a.value) / 2 * (float) Math.pow(2, 10 * (x - 1));

                x -= 1;

                return a.value + (b.value - a.value) / 2 * (float) (-Math.pow(2, -10 * x) + 2);
            }
        },
        BEZIER
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                float y1 = Interpolations.lerp(a.value, a.value + a.ry, x);
                float y2 = Interpolations.lerp(a.value + a.ry, b.value + b.ly, x);
                float y3 = Interpolations.lerp(b.value + b.ly, b.value, x);
                float y4 = Interpolations.lerp(y1, y2, x);
                float y5 = Interpolations.lerp(y2, y3, x);

                return Interpolations.lerp(y4, y5, x);
            }
        };

        public abstract float interpolate(Keyframe a, Keyframe b, float x);
    }

    public static enum Easing
    {
        IN, OUT, INOUT;
    }
}