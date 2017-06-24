package mchorse.aperture.events;

import mchorse.aperture.camera.CameraProfile;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Camera profile was changed event class
 * 
 * This class is responsible for notifying event bus handlers that given camera 
 * profile was changed.
 */
public class CameraProfileChangedEvent extends Event
{
    public CameraProfile profile;

    public CameraProfileChangedEvent(CameraProfile profile)
    {
        this.profile = profile;
    }
}