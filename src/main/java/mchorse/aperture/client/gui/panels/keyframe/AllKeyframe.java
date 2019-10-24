package mchorse.aperture.client.gui.panels.keyframe;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeInterpolation;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;

/**
 * All channel keyframe
 * 
 * This keyframe is responsible for delegating methods to actual 
 * keyframe
 */
public class AllKeyframe extends Keyframe
{
    public List<KeyframeCell> keyframes = new ArrayList<KeyframeCell>();

    public AllKeyframe(long tick)
    {
        super(tick, 0);
    }

    @Override
    public void setTick(long tick)
    {
        super.tick = tick;

        for (KeyframeCell cell : this.keyframes)
        {
            cell.keyframe.setTick(tick);
        }
    }

    /* Nope */
    @Override
    public void setValue(float value)
    {}

    @Override
    public void setEasing(Easing easing)
    {
        super.setEasing(easing);

        for (KeyframeCell cell : this.keyframes)
        {
            cell.keyframe.setEasing(easing);
        }
    }

    @Override
    public void setInterpolation(KeyframeInterpolation interp)
    {
        super.setInterpolation(interp);

        for (KeyframeCell cell : this.keyframes)
        {
            cell.keyframe.setInterpolation(interp);
        }
    }

    @Override
    public void copy(Keyframe keyframe) {
        long tick = this.tick;

        super.copy(keyframe);

        if (keyframe instanceof AllKeyframe)
        {
            AllKeyframe all = (AllKeyframe) keyframe;

            this.keyframes.clear();

            for (KeyframeCell cell : all.keyframes)
            {
                Keyframe frame = cell.channel.get(cell.channel.insert(tick, 0));
                frame.copy(cell.keyframe);
                frame.tick = tick;

                this.keyframes.add(new KeyframeCell(frame, cell.channel));
            }
        }
    }
}