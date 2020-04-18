package mchorse.aperture.client.gui.panels.modifiers.widgets;

import mchorse.aperture.camera.smooth.Envelope;
import mchorse.aperture.client.gui.panels.modifiers.GuiAbstractModifierPanel;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiInterpolationList;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

public class GuiEnvelope extends GuiElement
{
	public GuiAbstractModifierPanel panel;

	public GuiToggleElement enabled;
	public GuiToggleElement relative;
	public GuiIconElement pickInterp;
	public GuiTrackpadElement startX;
	public GuiTrackpadElement startD;
	public GuiTrackpadElement endX;
	public GuiTrackpadElement endD;
	public GuiInterpolationList interps;

	public GuiEnvelope(Minecraft mc, GuiAbstractModifierPanel panel)
	{
		super(mc);

		this.panel = panel;

		this.enabled = new GuiToggleElement(mc, I18n.format("aperture.gui.modifiers.enabled"), (b) ->
		{
			this.get().enabled = b.isToggled();
			this.panel.modifiers.editor.updateProfile();
		});
		this.relative = new GuiToggleElement(mc, I18n.format("aperture.gui.modifiers.relative"), (b) -> this.toggleRelative(b.isToggled()));
		this.pickInterp = new GuiIconElement(mc, Icons.GEAR, (b) -> this.interps.toggleVisible());
		this.pickInterp.flex().wh(20, 20);

		this.startX = new GuiTrackpadElement(mc, (value) ->
		{
			this.get().startX = value;
			this.panel.modifiers.editor.updateProfile();
		});
		this.startD = new GuiTrackpadElement(mc, (value) ->
		{
			this.get().startDuration = value;
			this.panel.modifiers.editor.updateProfile();
		});
		this.endX = new GuiTrackpadElement(mc, (value) ->
		{
			this.get().endX = value;
			this.panel.modifiers.editor.updateProfile();
		});
		this.endD = new GuiTrackpadElement(mc, (value) ->
		{
			this.get().endDuration = value;
			this.panel.modifiers.editor.updateProfile();
		});
		this.interps = new GuiInterpolationList(mc, (l) ->
		{
			this.get().interpolation = l.get(0);
			this.panel.modifiers.editor.updateProfile();
		});
		this.interps.setVisible(false);

		this.startX.limit(0);
		this.startD.limit(0);
		this.endX.limit(0);
		this.endD.limit(0);

		GuiElement row = Elements.row(mc, 5, 0, 20, this.enabled, this.relative, this.pickInterp);

		row.flex().relative(this.area).xy(5, 5).w(1F, -10);

		this.startX.flex().relative(this.enabled.area).xy(0, 20).w(1F).h(20);
		this.startD.flex().relative(this.startX.resizer()).xy(0, 25).w(1F).h(20);
		this.endX.flex().relative(this.relative.area).xy(0, 20).w(1F).h(20);
		this.endD.flex().relative(this.endX.resizer()).xy(0, 25).w(1F).h(20);
		this.interps.flex().relative(this.pickInterp.area).xy(1F, 1F).w(110).hTo(this.area, 1F).anchor(1F, 0F);

		this.add(row, this.startX, this.startD, this.endX, this.endD, this.interps);

		this.flex().h(90);
	}

	private void toggleRelative(boolean toggled)
	{
		Envelope envelope = this.get();

		envelope.relative = toggled;
		envelope.endX = this.getDuration() - envelope.endX;

		this.endX.setValue(this.get().endX);
		this.panel.modifiers.editor.updateProfile();
	}

	public long getDuration()
	{
		if (this.panel.modifiers.fixture == null)
		{
			return this.panel.modifiers.editor.getProfile().getDuration();
		}

		return this.panel.modifiers.fixture.getDuration();
	}

	public Envelope get()
	{
		return this.panel.modifier.envelope;
	}

	@Override
	public void resize()
	{
		super.resize();

		this.enabled.toggled(this.get().enabled);
		this.relative.toggled(this.get().relative);
		this.startX.setValue(this.get().startX);
		this.startD.setValue(this.get().startDuration);
		this.endX.setValue(this.get().endX);
		this.endD.setValue(this.get().endDuration);
		this.interps.setCurrent(this.get().interpolation);
	}

	@Override
	public void draw(GuiContext context)
	{
		if (this.interps.isVisible())
		{
			this.pickInterp.area.draw(0x88000000);
		}

		/* Draw an approximate visualisation of the envelope */
		Envelope envelope = this.get();
		long duration = this.getDuration();
		int startX = this.getX(envelope.getStartX(duration));
		int startD = this.getX(envelope.getStartDuration(duration));
		int endX = this.getX(envelope.getEndX(duration));
		int endD = this.getX(envelope.getEndDuration(duration));
		int y = this.area.ey();
		int h = 10;

		Gui.drawRect(this.area.x, y - 2, this.area.ex(), y, 0x66000000);
		Gui.drawRect(startX, y - h, startX + 1, y, 0xff000000);
		Gui.drawRect(startD, y - h, startD + 1, y, 0xff000000);
		Gui.drawRect(endD - 1, y - h, endD, y, 0xff000000);
		Gui.drawRect(endX - 1, y - h, endX, y, 0xff000000);

		super.draw(context);
	}

	private int getX(float value)
	{
		long duration = this.getDuration();
		float factor = value / (float) duration;

		return this.area.x + (int) (factor * this.area.w);
	}
}