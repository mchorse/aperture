package mchorse.aperture.client.gui.utils;

import mchorse.aperture.client.gui.dashboard.GuiCameraDashboard;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanel;
import net.minecraft.client.Minecraft;

public class GuiDebugPanel extends GuiDashboardPanel<GuiCameraDashboard>
{
	public GuiDebugPanel(Minecraft mc, GuiCameraDashboard dashboard)
	{
		super(mc, dashboard);
	}

	@Override
	public void draw(GuiContext context)
	{
		super.draw(context);

		this.drawCenteredString(this.font, "WARNING! DEBUG!", this.area.mx(), this.area.my() - 4, 0xffff00);
	}

	@Override
	public void drawBackground(GuiContext context)
	{}
}