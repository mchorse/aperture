package mchorse.aperture.events;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.config.GuiAbstractConfigOptions;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base class for all camera editor events 
 */
public abstract class CameraEditorEvent extends Event
{
    public final GuiCameraEditor editor;

    public CameraEditorEvent(GuiCameraEditor editor)
    {
        this.editor = editor;
    }

    /**
     * Camera editor initiate event 
     */
    public static class Init extends CameraEditorEvent
    {
        public Init(GuiCameraEditor editor)
        {
            super(editor);
        }
    }

    /**
     * Camera editor event when the playback scrub was scrubbed 
     */
    public static class Scrubbed extends CameraEditorEvent
    {
        /**
         * Whether camera runner is running current camera profile 
         */
        public boolean isRunning;

        /**
         * Position to which user scrubbed 
         */
        public int position;

        public Scrubbed(GuiCameraEditor editor, boolean isRunning, int position)
        {
            super(editor);

            this.isRunning = isRunning;
            this.position = position;
        }
    }

    /**
     * Camera editor event for notifying playback of the camera 
     */
    public static class Playback extends CameraEditorEvent
    {
        /**
         * Play is true and pause is false 
         */
        public boolean play;

        /**
         * Position at which camera editor started playing/was paused 
         */
        public int position;

        public Playback(GuiCameraEditor editor, boolean play, int position)
        {
            super(editor);

            this.play = play;
            this.position = position;
        }
    }

    /**
     * Camera editor event for loading camera options  
     */
    public static class Options extends CameraEditorEvent
    {
        public final List<GuiAbstractConfigOptions> options = new ArrayList<GuiAbstractConfigOptions>();

        public Options(GuiCameraEditor editor)
        {
            super(editor);
        }
    }
}