package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.minema.MinemaIntegration;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.modals.GuiMessageModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiModal;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDrawable;
import mchorse.mclib.client.gui.utils.Elements;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class GuiMinemaPanel extends GuiElement
{
	public GuiCameraEditor editor;

	public GuiElement fields;
	public GuiTextElement name;
	public GuiTrackpadElement left;
	public GuiTrackpadElement right;
	public GuiButtonElement setLeft;
	public GuiButtonElement setRight;
	public GuiToggleElement fixture;

	public GuiButtonElement record;

	private boolean recording;
	private boolean waiting;
	private int start;
	private int end;

	public GuiMinemaPanel(Minecraft mc, GuiCameraEditor editor)
	{
		super(mc);

		this.editor = editor;

		this.fields = new GuiElement(mc);
		this.name = new GuiTextElement(mc, (Consumer<String>) null);
		this.left = new GuiTrackpadElement(mc, (Consumer<Float>) null);
		this.left.limit(0).integer();
		this.left.setValue(0);
		this.right = new GuiTrackpadElement(mc, (Consumer<Float>) null);
		this.right.limit(0).integer();
		this.right.setValue(0);
		this.setLeft = new GuiButtonElement(mc, "Set start", this::calculateLeft);
		this.setRight = new GuiButtonElement(mc, "Set duration", this::calculateRight);
		this.fixture = new GuiToggleElement(mc, "Selected fixture only", null);
		this.record = new GuiButtonElement(mc, "Record", this::startRecording);

		this.fields.flex().relative(this.flex()).w(1F).column(5).vertical().stretch().height(20).padding(10);
		this.flex().hTo(this.fields.flex(), 1F);

		this.fields.add(Elements.label("Minema", 12).background(0x88000000));
		this.fields.add(this.name);
		this.fields.add(Elements.row(mc, 5, 0, 20, this.left, this.right));
		this.fields.add(Elements.row(mc, 5, 0, 20, this.setLeft, this.setRight));
		this.fields.add(this.fixture);
		this.fields.add(this.record);

		this.add(this.fields);
		this.add(new GuiDrawable((context) ->
		{
			if (this.fields.isVisible() && !this.name.isFocused() && this.name.field.getText().isEmpty())
			{
				this.font.drawStringWithShadow(this.getFilename(), this.name.area.x + 5, this.name.area.my() - 4, 0x888888);
			}
		}));

		this.fields.setVisible(MinemaIntegration.isLoaded() && MinemaIntegration.isAvailable());
	}

	public boolean isRecording()
	{
		return this.recording;
	}

	private boolean isRunning()
	{
		return this.editor.getRunner().isRunning();
	}

	private String getFilename()
	{
		String text = this.name.field.getText();

		if (!text.isEmpty())
		{
			return text;
		}

		text = this.editor.getProfile().getDestination().getFilename();

		if (this.fixture.isToggled())
		{
			AbstractFixture fixture = this.editor.getFixture();

			if (fixture != null)
			{
				text += "-" + (this.editor.getProfile().getAll().indexOf(fixture) + 1);
			}
		}

		return text;
	}

	private void calculateLeft(GuiButtonElement button)
	{
		int right = (int) (this.left.value + this.right.value);

		this.left.setValue(this.editor.timeline.value);
		this.right.setValue(right - this.left.value);
	}

	private void calculateRight(GuiButtonElement button)
	{
		this.right.setValue(this.editor.timeline.value - this.left.value);
	}

	private void startRecording(GuiButtonElement button)
	{
		if (this.isRunning() || MinemaIntegration.isRecording())
		{
			return;
		}

		/* Calculate start and end ticks */
		this.start = (int) this.left.value;
		this.end = this.start + (int) this.right.value;

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

		MinemaIntegration.setName(this.getFilename());

		try
		{
			MinemaIntegration.toggleRecording(true);
		}
		catch (Exception e)
		{
			GuiModal.addFullModal(this, () -> new GuiMessageModal(this.mc, MinemaIntegration.getMessage(e)));

			return;
		}

		this.editor.timeline.setValueFromScrub(this.start);
		this.editor.updatePlayer(this.start, 0);

		this.editor.root.setVisible(false);
		this.recording = this.waiting = true;
	}

	public void stop()
	{
		if (this.recording)
		{
			try
			{
				MinemaIntegration.toggleRecording(false);
			}
			catch (Exception e) {}

			if (this.isRunning())
			{
				this.editor.togglePlayback();
			}

			this.editor.root.setVisible(true);
			this.recording = this.waiting = false;
		}
	}

	/**
	 * Update the minema recording logic
	 */
	public void minema(int ticks, float partialTicks)
	{
		if (!this.recording)
		{
			return;
		}

		if (!MinemaIntegration.isRecording())
		{
			this.stop();

			GuiModal.addFullModal(this, () -> new GuiMessageModal(this.mc, "Minema prematurely stopped recording!\n\nPlease exit the camera editor and check the chat for more information..."));

			return;
		}

		if (this.waiting)
		{
			if (!this.isRunning() && partialTicks == 0)
			{
				this.editor.togglePlayback();
				this.waiting = false;
			}
		}
		else
		{
			if (this.isRunning() && ticks >= this.end)
			{
				this.editor.togglePlayback();
			}
			else if (!this.isRunning())
			{
				this.stop();
			}
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

		int x = this.area.x + 10;
		int y = this.area.my();

		if (!MinemaIntegration.isLoaded())
		{
			GuiDraw.drawMultiText(this.font, "Minema mod is not installed. Please install Minema 3.5 or above...", x, y, 0xffffff, this.area.w - 20, 12, 0.5F, 0.5F);
		}
		else if (!MinemaIntegration.isAvailable())
		{
			GuiDraw.drawMultiText(this.font, "Minema mod is installed, but it's outdated! This feature requires Minema 3.5 or above...", x, y, 0xffffff, this.area.w - 20, 12, 0.5F, 0.5F);
		}

		super.draw(context);
	}
}