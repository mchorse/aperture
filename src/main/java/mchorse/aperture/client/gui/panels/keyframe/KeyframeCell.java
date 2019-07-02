package mchorse.aperture.client.gui.panels.keyframe;

import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;

/**
 * Keyframe cell
 * 
 * Links a keyframe back to its parent channel
 */
public class KeyframeCell
{
    public Keyframe keyframe;
    public KeyframeChannel channel;

    public KeyframeCell(Keyframe keyframe, KeyframeChannel channel)
    {
        this.keyframe = keyframe;
        this.channel = channel;
    }
}