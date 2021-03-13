package mchorse.aperture.camera.fixtures;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.data.RenderFrame;
import mchorse.aperture.camera.values.ValueRenderFrames;
import mchorse.mclib.config.values.ValueFloat;
import mchorse.mclib.config.values.ValueInt;

import java.util.ArrayList;
import java.util.List;

public class ManualFixture extends AbstractFixture
{
    public final ValueFloat speed = new ValueFloat("speed", 1);
    public final ValueInt shift = new ValueInt("shift", 0);
    public final ValueRenderFrames frames = new ValueRenderFrames("frames");

    public List<RenderFrame> recorded = new ArrayList<RenderFrame>();

    public ManualFixture(long duration)
    {
        super(duration);

        this.register(this.speed);
        this.register(this.shift);
        this.register(this.frames);
    }

    /**
     * Get total duration of recorded data
     */
    public int getEndTick()
    {
        float speed = this.speed.get();

        if (speed <= 0)
        {
            return 1000000000;
        }

        return (int) ((this.frames.get().size() + this.shift.get()) / speed);
    }

    @Override
    public void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        List<List<RenderFrame>> frames = this.frames.get();
        int size = frames.size();

        if (size <= 0)
        {
            return;
        }

        float tick = ticks + previewPartialTick;
        int index = (int) ((int) tick * this.speed.get()) - this.shift.get();
        previewPartialTick = tick % 1;

        if (index < 0)
        {
            frames.get(0).get(0).apply(pos);

            return;
        }
        else if (index >= size)
        {
            List<RenderFrame> lastTick = frames.get(size - 1);

            lastTick.get(lastTick.size() - 1).apply(pos);

            return;
        }

        List<RenderFrame> lastTick = index - 1 >= 0 ? frames.get(index - 1) : null;
        RenderFrame last = lastTick == null || lastTick.isEmpty() ? null : lastTick.get(lastTick.size() - 1);
        float lastPt = last == null ? 0 : last.pt - 1;

        for (RenderFrame frame : frames.get(index))
        {
            if (frame.pt > previewPartialTick && previewPartialTick >= lastPt)
            {
                frame.apply(pos);

                return;
            }

            last = frame;
            lastPt = frame.pt;
        }

        if (last != null)
        {
            last.apply(pos);
        }
    }

    /**
     * Reorganize the recorded data in a much efficient structure
     */
    public List<List<RenderFrame>> setupRecorded()
    {
        if (this.recorded.isEmpty())
        {
            return null;
        }

        List<List<RenderFrame>> frames = new ArrayList<List<RenderFrame>>();

        List<RenderFrame> tick = new ArrayList<RenderFrame>();
        RenderFrame last = this.recorded.get(0);
        int lastTick = last.tick;

        for (RenderFrame frame : this.recorded)
        {
            if (frame.tick > lastTick)
            {
                frames.add(tick);

                /* Fill missing ticks */
                while (lastTick + 1 < frame.tick)
                {
                    tick = new ArrayList<RenderFrame>();
                    tick.add(last.copy());
                    lastTick += 1;

                    frames.add(tick);
                }

                lastTick = frame.tick;
                tick = new ArrayList<RenderFrame>();
            }

            tick.add(frame);
            last = frame;
        }

        if (!tick.isEmpty())
        {
            frames.add(tick);
        }

        this.recorded.clear();

        return frames;
    }

    @Override
    public AbstractFixture create(long duration)
    {
        return new ManualFixture(duration);
    }

    @Override
    protected void breakDownFixture(AbstractFixture original, long offset)
    {
        super.breakDownFixture(original, offset);

        this.shift.set((int) (this.shift.get() - offset * this.speed.get()));
    }
}