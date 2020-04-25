package mchorse.aperture.client.gui;

import info.ata4.minecraft.minema.MinemaAPI;
import mchorse.aperture.Aperture;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.minema.MinemaIntegration;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class GuiMinemaPanel extends GuiElement
{
	public GuiCameraEditor editor;

	public GuiTextElement name;
	public GuiTrackpadElement left;
	public GuiTrackpadElement right;
	public GuiButtonElement setLeft;
	public GuiButtonElement setRight;
	public GuiToggleElement fixture;

	public GuiButtonElement record;

	private boolean recording;
	private int start;
	private int end;

	public GuiMinemaPanel(Minecraft mc, GuiCameraEditor editor)
	{
		super(mc);

		this.editor = editor;

		this.name = new GuiTextElement(mc, (Consumer<String>) null);
		this.left = new GuiTrackpadElement(mc, (Consumer<Float>) null);
		this.left.limit(0).integer();
		this.right = new GuiTrackpadElement(mc, (Consumer<Float>) null);
		this.right.limit(0).integer();
		this.setLeft = new GuiButtonElement(mc, "Set start", this::calculateLeft);
		this.setRight = new GuiButtonElement(mc, "Set end", this::calculateRight);
		this.fixture = new GuiToggleElement(mc, "Selected fixture only", null);
		this.record = new GuiButtonElement(mc, "Record", this::startRecording);

		this.flex().column(5).vertical().stretch().height(20).padding(10);

		GuiLabel label = new GuiLabel(mc, "Minema").background(0x88000000);

		label.flex().h(12);

		this.add(label);
		this.add(this.name);
		this.add(Elements.row(mc, 5, 0, 20, this.left, this.right));
		this.add(Elements.row(mc, 5, 0, 20, this.setLeft, this.setRight));
		this.add(this.fixture);
		this.add(this.record);
	}

	public boolean isRecording()
	{
		return this.recording;
	}

	private boolean isRunning()
	{
		return this.editor.getRunner().isRunning();
	}

	private void calculateLeft(GuiButtonElement button)
	{
		this.left.setValue(this.editor.timeline.value);
	}

	private void calculateRight(GuiButtonElement button)
	{
		this.right.setValue(this.editor.timeline.value);
	}

	private void startRecording(GuiButtonElement button)
	{
		if (this.isRunning())
		{
			return;
		}

		/* Calculate start and end ticks */
		this.start = (int) this.left.value;
		this.end = (int) this.right.value;

		if (this.fixture.isToggled() && this.editor.panel.delegate != null)
		{
			AbstractFixture fixture = this.editor.panel.delegate.fixture;

			this.start = (int) this.editor.getProfile().calculateOffset(fixture);
			this.end = (int) (this.start + fixture.getDuration());
		}

		if (this.end - this.start <= 0)
		{
			return;
		}

		this.editor.timeline.setValueFromScrub(this.start);
		this.editor.updatePlayer(this.start, 0);

		MinemaAPI.toggleRecording(true);
		this.editor.root.setVisible(false);
		this.recording = true;
	}

	public void stop()
	{
		if (this.recording)
		{
			MinemaAPI.toggleRecording(false);

			if (this.isRunning())
			{
				this.editor.togglePlayback();
			}

			this.editor.root.setVisible(true);
			this.recording = false;
		}
	}

	public void process(int ticks, float partialTicks)
	{
		if (!this.recording)
		{
			return;
		}

		if (!this.isRunning() && partialTicks == 0)
		{
			this.editor.togglePlayback();
		}

		if (this.isRunning() && ticks >= this.end)
		{
			this.editor.togglePlayback();
		}
		else if (!this.isRunning() && ticks == this.end)
		{
			this.stop();
		}

		if (Aperture.debugTicks.get())
		{
			this.font.drawStringWithShadow(String.valueOf(ticks + partialTicks), 0, 0, 0xffffff);
		}
	}

	@Override
	public void draw(GuiContext context)
	{
		this.area.draw(0xaa000000);

		int x = this.area.mx();
		int y = this.area.my();

		if (!MinemaIntegration.isLoaded())
		{
			this.drawCenteredString(this.font, "Minema is not installed...", x, y - this.font.FONT_HEIGHT / 2, 0xffffff);
		}

		super.draw(context);
	}
}