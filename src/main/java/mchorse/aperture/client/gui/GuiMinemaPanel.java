package mchorse.aperture.client.gui;

import mchorse.aperture.camera.minema.MinemaIntegration;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import net.minecraft.client.Minecraft;

public class GuiMinemaPanel extends GuiElement
{
	public GuiCameraEditor editor;

	public GuiMinemaPanel(Minecraft mc, GuiCameraEditor editor)
	{
		super(mc);

		this.editor = editor;

		this.flex().h(40);
	}

	@Override
	public void draw(GuiContext context)
	{
		this.area.draw(0xaa000000);

		int x = this.area.mx();
		int y = this.area.my();

		if (MinemaIntegration.isLoaded())
		{
			this.drawCenteredString(this.font, "Minema is not installed...", x, y - this.font.FONT_HEIGHT / 2, 0xffffff);
		}

		super.draw(context);
	}
}