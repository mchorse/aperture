package mchorse.aperture.client.gui.dashboard;

import mchorse.mclib.client.gui.mclib.GuiDashboardPanel;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanels;
import net.minecraft.client.Minecraft;

public class GuiCameraPanels extends GuiDashboardPanels
{
	public GuiCameraDashboard dashboard;

	public GuiCameraPanels(Minecraft mc, GuiCameraDashboard dashboard)
	{
		super(mc);

		this.dashboard = dashboard;
	}

	@Override
	public void setPanel(GuiDashboardPanel panel)
	{
		super.setPanel(panel);

		this.dashboard.camera.setFlight(false);
	}
}