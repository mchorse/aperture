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

	public static boolean recording;
	public static int duration;
	public static int tick;

	public GuiButtonElement record;

	public static void update()
	{
		if (!recording)
		{
			return;
		}

		if (tick >= duration)
		{
			ClientProxy.openCameraEditor();
		}
		else
		{
			tick ++;
		}
	}

	public GuiManualFixturePanel(Minecraft mc, GuiCameraEditor editor)
	{
		super(mc, editor);

		this.record = new GuiButtonElement(mc, IKey.lang("aperture.gui.record"), this::startRecording);
		this.record.tooltip(IKey.lang(ENABLED));

		this.left.add(this.record);
	}

	private void startRecording(GuiButtonElement buttonElement)
	{
		this.fixture.list.clear();

		recording = true;
		duration = (int) this.fixture.getDuration();
		tick = 0;

		ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Playback(this.editor, true, (int) this.editor.getProfile().calculateOffset(this.fixture)));
		this.editor.exit();
	}

	@Override
	public void cameraEditorOpened()
	{
		if (recording)
		{
			recording = false;
			this.fixture.setupRecorded();
			this.editor.updateProfile();
		}
	}

	public void recordFrame(EntityPlayerSP player, float partialTicks)
	{
		ManualFixture.RenderFrame frame = new ManualFixture.RenderFrame();

		frame.fromPlayer(player, partialTicks);
		this.fixture.recorded.add(frame);
	}
}