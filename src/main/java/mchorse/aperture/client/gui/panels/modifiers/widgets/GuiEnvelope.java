package mchorse.aperture.client.gui.panels.modifiers.widgets;

import mchorse.aperture.camera.smooth.Envelope;
import mchorse.aperture.client.gui.panels.modifiers.GuiAbstractModifierPanel;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiInterpolationList;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Area;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Color;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class GuiEnvelope extends GuiElement
{
	public static final int regular = 0xff000000;
	public static final int selected = 0xffff0000;

	public GuiAbstractModifierPanel panel;

	public GuiToggleElement enabled;
	public GuiToggleElement relative;
	public GuiIconElement pickInterp;
	public GuiTrackpadElement startX;
	public GuiTrackpadElement startD;
	public GuiTrackpadElement endX;
	public GuiTrackpadElement endD;
	public GuiInterpolationList interps;

	private Color color = new Color();

	public GuiEnvelope(Minecraft mc, GuiAbstractModifierPanel panel)
	{
		super(mc);

		this.panel = panel;

		this.enabled = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.enabled"), (b) ->
		{
			this.get().enabled = b.isToggled();
			this.panel.modifiers.editor.updateProfile();
		});
		this.relative = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.relative"), (b) -> this.toggleRelative(b.isToggled()));
		this.pickInterp = new GuiIconElement(mc, Icons.GEAR, (b) -> this.interps.toggleVisible());

		this.startX = new GuiTrackpadElement(mc, (value) ->
		{
			this.get().startX = value.floatValue();
			this.panel.modifiers.editor.updateProfile();
		});
		this.startD = new GuiTrackpadElement(mc, (value) ->
		{
			this.get().startDuration = value.floatValue();
			this.panel.modifiers.editor.updateProfile();
		});
		this.endX = new GuiTrackpadElement(mc, (value) ->
		{
			this.get().endX = value.floatValue();
			this.panel.modifiers.editor.updateProfile();
		});
		this.endD = new GuiTrackpadElement(mc, (value) ->
		{
			this.get().endDuration = value.floatValue();
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

		this.enabled.flex().reset();
		this.relative.flex().reset();

		GuiElement row = Elements.row(mc, 5, 0, 20, this.enabled, this.relative, this.pickInterp);

		row.flex().relative(this).xy(10, 10).w(1F, -20);

		this.startX.flex().relative(this.enabled).xy(0, 20).w(1F);
		this.startD.flex().relative(this.startX).xy(0, 25).w(1F);
		this.endX.flex().relative(this.relative).xy(0, 20).w(1F);
		this.endD.flex().relative(this.endX).xy(0, 25).w(1F);
		this.interps.flex().relative(this.pickInterp).xy(1F, 1F).w(110).hTo(this.area, 1F).anchor(1F, 0F);

		this.add(row, this.startX, this.startD, this.endX, this.endD, this.interps);

		this.flex().h(95);
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
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		GL11.glLineWidth(2);
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		this.drawGraph(context, buffer, this.get());
		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glLineWidth(1);

		/* Draw cursor */
		int x = this.panel.modifiers.editor.timeline.value;

		if (this.panel.modifiers.fixture != null)
		{
			x -= this.panel.modifiers.editor.getProfile().calculateOffset(this.panel.modifiers.fixture);
		}

		float factor = this.get().factor(this.getDuration(), x);

		x = this.getX(x);

		if (x >= this.area.x && x < this.area.ex())
		{
			Gui.drawRect(x, this.area.ey() - 3 - (int) (9 * factor), x + 2, this.area.ey(), 0xff57f52a);
		}

		super.draw(context);
	}

	/**
	 * Draw the graph at the bottom of the envelope panel
	 */
	private void drawGraph(GuiContext context, BufferBuilder buffer, Envelope envelope)
	{
		long duration = this.getDuration();
		int startX = this.getX(envelope.getStartX(duration));
		int startD = this.getX(envelope.getStartDuration(duration));
		int endX = this.getX(envelope.getEndX(duration));
		int endD = this.getX(envelope.getEndDuration(duration));
		int sy = this.area.ey() - 1;
		int ey = sy - 10;

		this.color.set(this.startX.area.isInside(context) ? selected : regular, true);

		if (startX >= this.area.x)
		{
			buffer.pos(this.area.x, sy,0).color(this.color.r, this.color.g, this.color.b, this.color.a).endVertex();
		}

		if (startX != startD)
		{
			buffer.pos(startX, sy,0).color(this.color.r, this.color.g, this.color.b, this.color.a).endVertex();
		}

		if (envelope.startDuration > 0)
		{
			this.drawFades(context, buffer, envelope.interpolation, this.startD.area, startX, startD, sy, ey);
		}

		this.color.set(this.startD.area.isInside(context) ? selected : regular, true);
		buffer.pos(startD, ey,0).color(this.color.r, this.color.g, this.color.b, this.color.a).endVertex();
		this.color.set(regular, true);
		buffer.pos(Interpolations.lerp(startD, endD, 0.5F), ey,0).color(this.color.r, this.color.g, this.color.b, this.color.a).endVertex();
		this.color.set(this.endD.area.isInside(context) ? selected : regular, true);
		buffer.pos(endD, ey,0).color(this.color.r, this.color.g, this.color.b, this.color.a).endVertex();

		if (envelope.endDuration > 0)
		{
			this.drawFades(context, buffer, envelope.interpolation, this.endX.area, endD, endX, ey, sy);
		}

		if (endD != endX)
		{
			this.color.set(this.endX.area.isInside(context) ? selected : regular, true);
			buffer.pos(endX, sy,0).color(this.color.r, this.color.g, this.color.b, this.color.a).endVertex();
		}

		if (endX < this.area.ex())
		{
			buffer.pos(this.area.ex(), sy,0).color(this.color.r, this.color.g, this.color.b, this.color.a).endVertex();
		}
	}

	/**
	 * Draw fading lines for the graph
	 */
	private void drawFades(GuiContext context, BufferBuilder buffer, Interpolation interp, Area area, int startX, int startD, int a, int b)
	{
		for (int i = 1; i < 10; i ++)
		{
			float x = Interpolations.lerp(startX, startD, i / 10F);
			float y = interp.interpolate(a, b, i / 10F);

			if (i == 6)
			{
				this.color.set(area.isInside(context) ? selected : regular, true);
			}

			buffer.pos(x, y,0).color(this.color.r, this.color.g, this.color.b, this.color.a).endVertex();
		}
	}

	private int getX(float value)
	{
		long duration = this.getDuration();
		float factor = value / (float) duration;

		return this.area.x + (int) (factor * this.area.w);
	}
}