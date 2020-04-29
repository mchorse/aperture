package mchorse.aperture.client.gui.panels;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.fixtures.ManualFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.events.CameraEditorEvent;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class GuiManualFixturePanel extends GuiAbstractFixturePanel<ManualFixture>
{
	private static final String ENABLED = "aperture.gui.panels.manual";
	private static final String DISABLED = "aperture.gui.panels.constant_framerate";
	private static final String VSYNC = "aperture.gui.panels.vsync";

	public static boolean recording;

	public GuiButtonElement record;

	public GuiManualFixturePanel(Minecraft mc, GuiCameraEditor editor)
	{
		super(mc, editor);

		this.record = new GuiButtonElement(mc, IKey.lang("aperture.gui.record"), this::startRecording);
		this.record.tooltip(IKey.lang(ENABLED));

		this.left.add(this.record);
	}

	@Override
	public void select(ManualFixture fixture, long duration)
	{
		super.select(fixture, duration);

		if (recording)
		{
			recording = false;
		}

		int frameRate = this.mc.gameSettings.limitFramerate;
		boolean vsync = this.mc.gameSettings.enableVsync;
		boolean constant = frameRate > 0 && frameRate <= 200 && !vsync;

		this.record.setEnabled(constant);
		this.record.tooltip.label.set(constant ? ENABLED : (vsync ? VSYNC : DISABLED));
	}

	private void startRecording(GuiButtonElement buttonElement)
	{
		this.fixture.framerate = this.mc.gameSettings.limitFramerate;
		this.fixture.list.clear();

		recording = true;

		ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Playback(this.editor, true, (int) this.editor.getProfile().calculateOffset(this.fixture)));
		this.editor.exit();
	}

	public void recordFrame(EntityPlayerSP player, float partialTicks)
	{
		ManualFixture.RenderFrame frame = new ManualFixture.RenderFrame();

		frame.fromPlayer(player, partialTicks);
		this.fixture.list.add(frame);
	}
}