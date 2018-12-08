package mchorse.aperture.events;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.config.AbstractGuiConfigOptions;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Camera editor options event 
 * 
 * This event allows dynamically load up camera options classes from 
 * other mods.
 */
public class CameraEditorOptionsEvent extends Event
{
    public final List<AbstractGuiConfigOptions> options = new ArrayList<AbstractGuiConfigOptions>();
    public final GuiCameraEditor editor;

    public CameraEditorOptionsEvent(GuiCameraEditor editor)
    {
        this.editor = editor;
    }
}