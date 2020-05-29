package mchorse.aperture.client.gui.panels.keyframe;

import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;

public class AllKeyframeChannel extends KeyframeChannel
{
    public KeyframeFixture fixture;

    @Override
    protected Keyframe create(long tick, double value)
    {
        return new AllKeyframe(tick);
    }

    public void setFixture(KeyframeFixture fixture)
    {
        this.fixture = fixture;

        this.keyframes.clear();

        for (KeyframeChannel channel : fixture.channels)
        {
            for (Keyframe kf : channel.getKeyframes())
            {
                int index = this.insert(kf.tick, 0);
                AllKeyframe allStar = (AllKeyframe) this.keyframes.get(index);

                allStar.keyframes.add(new KeyframeCell(kf, channel));
                allStar.easing = kf.easing;
                allStar.interp = kf.interp;
            }
        }
    }

    @Override
    public void sort()
    {
        super.sort();

        for (KeyframeChannel channel : this.fixture.channels)
        {
            channel.sort();
        }
    }

    @Override
    public void remove(int index)
    {
        AllKeyframe kf = (AllKeyframe) this.keyframes.remove(index);

        for (KeyframeCell cell : kf.keyframes)
        {
            cell.channel.remove(cell.channel.getKeyframes().indexOf(cell.keyframe));
        }
    }
}