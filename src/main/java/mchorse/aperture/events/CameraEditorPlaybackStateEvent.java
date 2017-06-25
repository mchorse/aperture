package mchorse.aperture.events;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Camera editor playback state event
 * 
 * This event is fired when the camera editor is either played or paused.
 */
public class CameraEditorPlaybackStateEvent extends Event
{
    /**
     * Play is true and pause is false 
     */
    public boolean play;

    /**
     * Position at which camera editor started playing/was paused 
     */
    public int position;

    public CameraEditorPlaybackStateEvent(boolean play, int position)
    {
        this.play = play;
        this.position = position;
    }
}