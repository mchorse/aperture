package mchorse.aperture.camera.fixtures;

import java.util.ArrayList;
import java.util.List;

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

    public static class KeyframeChannel
    {
        @Expose
        public final List<Keyframe> keyframes = new ArrayList<Keyframe>();

        public boolean isEmpty()
        {
            return this.keyframes.isEmpty();
        }

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
                    float x = (ticks - prev.tick) / (frame.tick - prev.tick);

                    return Interpolations.lerp(prev.value, frame.value, x);
                }

                prev = frame;
            }

            return prev.value;
        }

        public void insert(long tick, float value)
        {
            Keyframe prev = null;
            int index = 0;

            for (Keyframe frame : this.keyframes)
            {
                if (frame.tick == tick)
                {
                    frame.value = value;

                    return;
                }

                if (prev != null && tick > prev.tick && tick < frame.tick)
                {
                    break;
                }

                index++;
                prev = frame;
            }

            this.keyframes.add(index, new Keyframe(tick, value));
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

    public static class Keyframe
    {
        @Expose
        public long tick;

        @Expose
        public float value;

        public Keyframe(long tick, float value)
        {
            this.tick = tick;
            this.value = value;
        }

        public Keyframe clone()
        {
            Keyframe keyframe = new Keyframe(this.tick, this.value);

            return keyframe;
        }

        public void toByteBuf(ByteBuf buffer)
        {
            buffer.writeLong(this.tick);
            buffer.writeFloat(this.value);
        }
    }
}