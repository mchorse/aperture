package mchorse.aperture.events;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Camera editor scrubbed event
 * 
 * This event fired when user scrubs (drags the cursor) over the timeline.
 */
public class CameraEditorScrubbedEvent extends Event
{
    /**
     * Whether camera runner is running current camera profile 
     */
    public boolean isRunning;

    /**
     * Position to which user scrubbed 
     */
    public int position;

    public CameraEditorScrubbedEvent(boolean isRunning, int position)
    {
        this.isRunning = isRunning;
        this.position = position;
    }
}