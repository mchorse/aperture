package mchorse.aperture.client.gui.utils;

import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.Icons;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class GuiTextHelpElement extends GuiTextElement
{
	public GuiIconElement help;
	public String link = "";

	public GuiTextHelpElement(Minecraft mc, int maxLength, Consumer<String> callback)
	{
		super(mc, maxLength, callback);

		this.setup(mc);
	}

	public GuiTextHelpElement(Minecraft mc, Consumer<String> callback)
	{
		super(mc, callback);

		this.setup(mc);
	}

	protected void setup(Minecraft mc)
	{
		this.help = new GuiIconElement(mc, Icons.HELP, (b) -> GuiUtils.openWebLink(this.link));
		this.help.flex().relative(this).x(1F, -1).y(1).wh(18, 18).anchorX(1F);
		this.help.hoverColor(0xff999999).iconColor(0xffcccccc);
		this.add(this.help);
	}

	public GuiTextHelpElement link(String link)
	{
		this.link = link;

		return this;
	}
}