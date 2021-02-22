package mchorse.aperture.camera.fixtures;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.data.RenderFrame;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * Keyframe fixture
 * 
 * This fixture provides a much flexible control over camera, allowing setting 
 * up different transitions between points with different easing.
 */
public class KeyframeFixture extends AbstractFixture
{
    /* Different animated channels */

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

            if (path.getPoints().size() > 1)
            {
                KeyframeFixture kf = path.toKeyframe();

                this.modifiers.clear();
                this.copy(kf);
            }
        }
        else if (from instanceof ManualFixture)
        {
            ManualFixture fixture = (ManualFixture) from;
            List<List<RenderFrame>> ticks = fixture.frames.get();

            for (int i = 0, c = ticks.size(); i < c; i++)
            {
                List<RenderFrame> frames = ticks.get(i);

                if (frames != null && frames.size() > 0)
                {
                    RenderFrame frame = frames.get(0);

                    this.x.insert(i, frame.x);
                    this.y.insert(i, frame.y);
                    this.z.insert(i, frame.z);
                    this.yaw.insert(i, frame.yaw);
                    this.pitch.insert(i, frame.pitch);
                    this.roll.insert(i, frame.roll);
                    this.fov.insert(i, frame.fov);
                }
            }
        }
    }

    @Override
    public void fromJSON(JsonObject object)
    {
        super.fromJSON(object);

        this.x.sort();
        this.y.sort();
        this.z.sort();
        this.yaw.sort();
        this.pitch.sort();
        this.roll.sort();
        this.fov.sort();
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.x.fromBytes(buffer);
        this.y.fromBytes(buffer);
        this.z.fromBytes(buffer);
        this.yaw.fromBytes(buffer);
        this.pitch.fromBytes(buffer);
        this.roll.fromBytes(buffer);
        this.fov.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.x.toBytes(buffer);
        this.y.toBytes(buffer);
        this.z.toBytes(buffer);
        this.yaw.toBytes(buffer);
        this.pitch.toBytes(buffer);
        this.roll.toBytes(buffer);
        this.fov.toBytes(buffer);
    }
}