package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.utils.undo.IUndo;

public abstract class CameraProfileUndo implements IUndo<CameraProfile>
{
    public int cursor;

    public IUndo<CameraProfile> cursor(int timelineCursor)
    {
        this.cursor = timelineCursor;

        return this;
    }
}
