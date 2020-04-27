package mchorse.aperture.camera.fixtures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

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
    public final KeyframeChannel x;

    @Expose
    public final KeyframeChannel y;

    @Expose
    public final KeyframeChannel z;

    @Expose
    public final KeyframeChannel yaw;

    @Expose
    public final KeyframeChannel pitch;

    @Expose
    public final KeyframeChannel roll;

    @Expose
    public final KeyframeChannel fov;

    public KeyframeChannel[] channels;

    public KeyframeFixture()
    {
        super(0);

        this.x = new KeyframeChannel();
        this.y = new KeyframeChannel();
        this.z = new KeyframeChannel();
        this.yaw = new KeyframeChannel();
        this.pitch = new KeyframeChannel();
        this.roll = new KeyframeChannel();
        this.fov = new KeyframeChannel();
        this.channels = new KeyframeChannel[] {this.x, this.y, this.z, this.yaw, this.pitch, this.roll, this.fov};
    }

    public KeyframeFixture(long duration)
    {
        super(duration);

        this.x = new KeyframeChannel();
        this.y = new KeyframeChannel();
        this.z = new KeyframeChannel();
        this.yaw = new KeyframeChannel();
        this.pitch = new KeyframeChannel();
        this.roll = new KeyframeChannel();
        this.fov = new KeyframeChannel();
        this.channels = new KeyframeChannel[] {this.x, this.y, this.z, this.yaw, this.pitch, this.roll, this.fov};
    }

    @Override
    public void initiate()
    {
        for (KeyframeChannel channel : this.channels)
        {
            channel.sort();
        }
    }

    @Override
    public void fromPlayer(EntityPlayer player)
    {
        Position pos = new Position(player);

        this.x.insert(0, (float) pos.point.x);
        this.y.insert(0, (float) pos.point.y);
        this.z.insert(0, (float) pos.point.z);
        this.yaw.insert(0, pos.angle.yaw);
        this.pitch.insert(0, pos.angle.pitch);
        this.roll.insert(0, pos.angle.roll);
        this.fov.insert(0, pos.angle.fov);
    }

    @Override
    public void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        float t = ticks + previewPartialTick;

        if (!this.x.isEmpty()) pos.point.x = this.x.interpolate(t);
        if (!this.y.isEmpty()) pos.point.y = this.y.interpolate(t);
        if (!this.z.isEmpty()) pos.point.z = this.z.interpolate(t);
        if (!this.yaw.isEmpty()) pos.angle.yaw = this.yaw.interpolate(t);
        if (!this.pitch.isEmpty()) pos.angle.pitch = this.pitch.interpolate(t);
        if (!this.roll.isEmpty()) pos.angle.roll = this.roll.interpolate(t);
        if (!this.fov.isEmpty()) pos.angle.fov = this.fov.interpolate(t);
    }

    @Override
    public AbstractFixture copy()
    {
        KeyframeFixture fixture = new KeyframeFixture(this.duration);

        AbstractFixture.copyModifiers(this, fixture);
        fixture.name = this.name;
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
        protected final List<Keyframe> keyframes = new ArrayList<Keyframe>();

        protected Keyframe create(long tick, float value)
        {
            return new Keyframe(tick, value);
        }

        public boolean isEmpty()
        {
            return this.keyframes.isEmpty();
        }

        public List<Keyframe> getKeyframes()
        {
            return this.keyframes;
        }

        public boolean has(int index)
        {
            return index >= 0 && index < this.keyframes.size();
        }

        public Keyframe get(int index)
        {
            return this.has(index) ? this.keyframes.get(index) : null;
        }

        public void remove(int index)
        {
            if (index < 0 || index > this.keyframes.size() - 1)
            {
                return;
            }

            Keyframe frame = this.keyframes.remove(index);

            frame.prev.next = frame.next;
            frame.next.prev = frame.prev;
        }

        /**
         * Calculate the value at given tick 
         */
        public float interpolate(float ticks)
        {
            if (this.keyframes.isEmpty())
            {
                return 0;
            }

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
            Keyframe prev = null;

            if (!this.keyframes.isEmpty())
            {
                prev = this.keyframes.get(0);

                if (tick < prev.tick)
                {
                    this.keyframes.add(0, this.create(tick, value));

                    return 0;
                }
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

            Keyframe frame = this.create(tick, value);
            this.keyframes.add(index, frame);

            if (this.keyframes.size() > 1)
            {
                frame.prev = this.keyframes.get(Math.max(index - 1, 0));
                frame.next = this.keyframes.get(Math.min(index + 1, this.keyframes.size() - 1));
            }

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

            if (!this.keyframes.isEmpty())
            {
                Keyframe prev = this.keyframes.get(0);

                for (Keyframe frame : this.keyframes)
                {
                    frame.prev = prev;
                    prev.next = frame;

                    prev = frame;
                }

                prev.next = prev;
            }
        }

        public void copy(KeyframeChannel channel)
        {
            this.keyframes.clear();

            for (Keyframe frame : channel.keyframes)
            {
                this.keyframes.add(frame.copy());
            }

            this.sort();
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

            this.sort();
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
        public Keyframe prev;
        public Keyframe next;

        @Expose
        public long tick;

        @Expose
        public float value;

        @Expose
        public KeyframeInterpolation interp = KeyframeInterpolation.LINEAR;

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

            this.prev = this;
            this.next = this;
        }

        public void setTick(long tick)
        {
            this.tick = tick;
        }

        public void setValue(float value)
        {
            this.value = value;
        }

        public void setInterpolation(KeyframeInterpolation interp)
        {
            this.interp = interp;
        }

        public void setInterpolation(KeyframeInterpolation interp, Easing easing)
        {
            this.interp = interp;
            this.setEasing(easing);
        }

        public void setEasing(Easing easing)
        {
            this.easing = easing;
        }

        public float interpolate(Keyframe frame, float x)
        {
            return this.interp.interpolate(this, frame, x);
        }

        public Keyframe copy()
        {
            Keyframe frame = new Keyframe(this.tick, this.value);

            frame.copy(this);

            return frame;
        }

        public void copy(Keyframe keyframe)
        {
            this.tick = keyframe.tick;
            this.value = keyframe.value;
            this.interp = keyframe.interp;
            this.easing = keyframe.easing;
            this.lx = keyframe.lx;
            this.ly = keyframe.ly;
            this.rx = keyframe.rx;
            this.ry = keyframe.ry;
        }

        public void fromByteBuf(ByteBuf buffer)
        {
            this.interp = KeyframeInterpolation.values()[buffer.readInt()];
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

    public static enum KeyframeInterpolation
    {
        CONST("const")
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                return a.value;
            }
        },
        LINEAR("linear")
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                return Interpolations.lerp(a.value, b.value, x);
            }
        },
        QUAD("quad")
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
        CUBIC("cubic")
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
        HERMITE("hermite")
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                double v0 = a.prev.value;
                double v1 = a.value;
                double v2 = b.value;
                double v3 = b.next.value;

                return (float) Interpolations.cubicHermite(v0, v1, v2, v3, x);
            }
        },
        EXP("exp")
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
        BEZIER("bezier")
        {
            @Override
            public float interpolate(Keyframe a, Keyframe b, float x)
            {
                if (x <= 0) return a.value;
                if (x >= 1) return b.value;

                /* Transform input to 0..1 */
                float w = b.tick - a.tick;
                float h = b.value - a.value;

                /* In case if there is no slope whatsoever */
                if (h == 0) h = 0.00001F;

                float x1 = a.rx / w;
                float y1 = a.ry / h;
                float x2 = (w - b.lx) / w;
                float y2 = (h + b.ly) / h;
                float e = 0.0005F;

                e = h == 0 ? e : Math.max(Math.min(e, 1 / h * e), 0.00001F);
                x1 = MathHelper.clamp(x1, 0, 1);
                x2 = MathHelper.clamp(x2, 0, 1);

                return Interpolations.bezier(0, y1, y2, 1, Interpolations.bezierX(x1, x2, x, e)) * h + a.value;
            }
        };

        public final String key;

        private KeyframeInterpolation(String key)
        {
            this.key = key;
        }

        public abstract float interpolate(Keyframe a, Keyframe b, float x);

	    public String getKey()
        {
            return "aperture.gui.panels.interps." + this.key;
        }
    }

    public static enum Easing
    {
        IN, OUT, INOUT;
    }
}