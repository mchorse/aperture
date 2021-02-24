package mchorse.aperture.camera.fixtures;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.data.RenderFrame;
import mchorse.aperture.camera.values.ValueKeyframeChannel;
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

    public final ValueKeyframeChannel x = new ValueKeyframeChannel("x");
    public final ValueKeyframeChannel y = new ValueKeyframeChannel("y");
    public final ValueKeyframeChannel z = new ValueKeyframeChannel("z");
    public final ValueKeyframeChannel yaw = new ValueKeyframeChannel("yaw");
    public final ValueKeyframeChannel pitch = new ValueKeyframeChannel("pitch");
    public final ValueKeyframeChannel roll = new ValueKeyframeChannel("roll");
    public final ValueKeyframeChannel fov = new ValueKeyframeChannel("fov");

    public ValueKeyframeChannel[] channels;

    public KeyframeFixture(long duration)
    {
        super(duration);

        this.channels = new ValueKeyframeChannel[] {this.x, this.y, this.z, this.yaw, this.pitch, this.roll, this.fov};

        for (ValueKeyframeChannel channel : this.channels)
        {
            this.register(channel);
        }
    }

    @Override
    public void initiate()
    {
        for (ValueKeyframeChannel channel : this.channels)
        {
            channel.get().sort();
        }
    }

    @Override
    public void fromPlayer(EntityPlayer player)
    {
        Position pos = new Position(player);

        this.x.get().insert(0, pos.point.x);
        this.y.get().insert(0, pos.point.y);
        this.z.get().insert(0, pos.point.z);
        this.yaw.get().insert(0, pos.angle.yaw);
        this.pitch.get().insert(0, pos.angle.pitch);
        this.roll.get().insert(0, pos.angle.roll);
        this.fov.get().insert(0, pos.angle.fov);
    }

    @Override
    public void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        float t = ticks + previewPartialTick;

        if (!this.x.get().isEmpty()) pos.point.x = this.x.get().interpolate(t);
        if (!this.y.get().isEmpty()) pos.point.y = this.y.get().interpolate(t);
        if (!this.z.get().isEmpty()) pos.point.z = this.z.get().interpolate(t);
        if (!this.yaw.get().isEmpty()) pos.angle.yaw = (float) this.yaw.get().interpolate(t);
        if (!this.pitch.get().isEmpty()) pos.angle.pitch = (float) this.pitch.get().interpolate(t);
        if (!this.roll.get().isEmpty()) pos.angle.roll = (float) this.roll.get().interpolate(t);
        if (!this.fov.get().isEmpty()) pos.angle.fov = (float) this.fov.get().interpolate(t);
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

                    this.x.get().insert(i, frame.x);
                    this.y.get().insert(i, frame.y);
                    this.z.get().insert(i, frame.z);
                    this.yaw.get().insert(i, frame.yaw);
                    this.pitch.get().insert(i, frame.pitch);
                    this.roll.get().insert(i, frame.roll);
                    this.fov.get().insert(i, frame.fov);
                }
            }
        }
    }
}