package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.client.gui.GuiPlaybackScrub;
import mchorse.aperture.utils.undo.IUndo;

public abstract class CameraProfileUndo implements IUndo<CameraProfile>
{
    public int cursor;
    public double viewMin;
    public double viewMax;

    public IUndo<CameraProfile> view(GuiPlaybackScrub scrub)
    {
        this.cursor = scrub.value;
        this.viewMin = scrub.scale.getMinValue();
        this.viewMax = scrub.scale.getMaxValue();

        return this;
    }
}
