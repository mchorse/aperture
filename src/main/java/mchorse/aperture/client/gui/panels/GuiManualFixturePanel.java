package mchorse.aperture.client.gui.panels;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.fixtures.ManualFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.utils.APIcons;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class GuiManualFixturePanel extends GuiAbstractFixturePanel<ManualFixture>
{
	private static final String ENABLED = "aperture.gui.panels.manual";

	public static boolean recording;
	public static int duration;
	public static int tick;
	public static int offset;
	public static Timer timer = new Timer(3000);

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

	/**
	 * Draw manual fixture related HUD elements, like countdown and
	 * recording overlays
	 */
	public static void drawHUD(int w, int h)
	{
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;

		if (timer.checkReset())
		{
			recording = true;
			ClientProxy.getCameraEditor().postPlayback(offset);
		}
		else if (timer.enabled)
		{
			long remaining = timer.getRemaining();
			float factor = (remaining % 1000L) / 1000F * 3;

			GlStateManager.pushMatrix();
			GlStateManager.translate(w / 2, h / 2, 0);
			GlStateManager.scale(factor, factor, 1);

			String label = String.valueOf(remaining / 1000L + 1);
			font.drawStringWithShadow(label, -font.getStringWidth(label) / 2, -4, 0xffffff);

			GlStateManager.popMatrix();
		}

		if (recording)
		{
			String caption = "Recording§r (§l" + tick + "§r)";

			APIcons.RECORD.render(4, 4, 0, 0);
			font.drawStringWithShadow(caption, 22, 8, 0xffffffff);
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
		offset = (int) this.editor.getProfile().calculateOffset(this.fixture);
		duration = (int) this.fixture.getDuration();
		tick = 0;
		timer.mark();

		this.editor.postRewind(offset);
		this.editor.exit();
	}

	@Override
	public void cameraEditorOpened()
	{
		if (recording)
		{
			recording = false;

			if (tick > 0)
			{
				this.fixture.setupRecorded();
				this.editor.updateProfile();
			}
		}
		else
		{
			timer.reset();
		}
	}

	public void recordFrame(EntityPlayerSP player, float partialTicks)
	{
		this.fixture.recorded.add(new ManualFixture.RenderFrame(player, partialTicks));
	}
}