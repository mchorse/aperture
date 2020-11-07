package mchorse.aperture.events;

import mchorse.aperture.client.gui.dashboard.GuiCameraDashboard;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base class for all camera editor events
 */
public abstract class CameraEditorDashboardEvent extends Event
{
	public final GuiCameraDashboard editor;

	public CameraEditorDashboardEvent(GuiCameraDashboard editor)
	{
		this.editor = editor;
	}

	/**
	 * Camera editor event for registering panels
	 */
	public static class RegisteringPanels extends CameraEditorDashboardEvent
	{
		public RegisteringPanels(GuiCameraDashboard editor)
		{
			super(editor);
		}
	}

	/**
	 * Camera editor event for unregistering panels
	 */
	public static class RemovingPanels extends CameraEditorDashboardEvent
	{
		public RemovingPanels(GuiCameraDashboard editor)
		{
			super(editor);
		}
	}
}
