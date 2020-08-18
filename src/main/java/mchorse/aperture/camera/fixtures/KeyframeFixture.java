package mchorse.aperture.camera.fixtures;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
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

        this.x.insert(0, pos.point.x);
        this.y.insert(0, pos.point.y);
        this.z.insert(0, pos.point.z);
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
        if (!this.yaw.isEmpty()) pos.angle.yaw = (float) this.yaw.interpolate(t);
        if (!this.pitch.isEmpty()) pos.angle.pitch = (float) this.pitch.interpolate(t);
        if (!this.roll.isEmpty()) pos.angle.roll = (float) this.roll.interpolate(t);
        if (!this.fov.isEmpty()) pos.angle.fov = (float) this.fov.interpolate(t);
    }

    @Override
    public AbstractFixture create(long duration)
    {
        return new KeyframeFixture(duration);
    }

    @Override
    public void copy(AbstractFixture from)
    {
        super.copy(from);

        if (from instanceof KeyframeFixture)
        {
            KeyframeFixture keyframe = (KeyframeFixture) from;

            this.x.copy(keyframe.x);
            this.y.copy(keyframe.y);
            this.z.copy(keyframe.z);
            this.yaw.copy(keyframe.yaw);
            this.pitch.copy(keyframe.pitch);
            this.roll.copy(keyframe.roll);
            this.fov.copy(keyframe.fov);
        }
    }

    @Override
    public void copyByReplacing(AbstractFixture from)
    {
        if (from instanceof DollyFixture)
        {
            PathFixture path = new PathFixture(from.getDuration());

            path.copyByReplacing(from);
            this.copyByReplacing(path);

            return;
        }

        super.copyByReplacing(from);

        if (from instanceof PathFixture)
        {
            PathFixture path = (PathFixture) from;
            KeyframeFixture kf = path.toKeyframe();

            this.copy(kf);
        }
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
}