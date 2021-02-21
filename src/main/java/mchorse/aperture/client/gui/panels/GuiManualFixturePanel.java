package mchorse.aperture.client.gui.panels;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.fixtures.ManualFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.utils.APIcons;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

public class GuiManualFixturePanel extends GuiAbstractFixturePanel<ManualFixture>
{
    public static boolean recording;
    public static int duration;
    public static int tick;
    public static int offset;
    public static Timer timer = new Timer(3000);

    public GuiTrackpadElement shift;
    public GuiTrackpadElement speed;
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
            ClientProxy.getCameraEditor().postPlayback(offset, true);
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

        this.shift = new GuiTrackpadElement(mc, (v) -> this.fixture.shift = v.intValue());
        this.shift.integer().tooltip(IKey.lang("aperture.gui.panels.manual.shift"));

        this.speed = new GuiTrackpadElement(mc, (v) -> this.fixture.speed = v.floatValue());
        this.speed.limit(0).tooltip(IKey.lang("aperture.gui.panels.manual.speed"));

        this.record = new GuiButtonElement(mc, IKey.lang("aperture.gui.record"), this::startRecording);
        this.record.tooltip(IKey.lang("aperture.gui.panels.manual.record"));

        this.left.add(Elements.label(IKey.lang("aperture.gui.panels.manual.title")).background(0x88000000), this.shift, this.speed, this.record);

        this.keys().register(IKey.lang("aperture.gui.panels.keys.record_manual"), Keyboard.KEY_R, () -> this.record.clickItself(GuiBase.getCurrent())).held(Keyboard.KEY_LCONTROL).active(editor::isFlightDisabled).category(CATEGORY);
    }

    @Override
    public void select(ManualFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.shift.setValue(fixture.shift);
        this.speed.setValue(fixture.speed);
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
        super.cameraEditorOpened();

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