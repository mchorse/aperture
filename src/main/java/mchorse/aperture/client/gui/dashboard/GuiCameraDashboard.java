package mchorse.aperture.client.gui.dashboard;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiPlaybackScrub;
import mchorse.aperture.client.gui.utils.GuiDebugPanel;
import mchorse.aperture.events.CameraEditorDashboardEvent;
import mchorse.aperture.utils.APIcons;
import mchorse.mclib.McLib;
import mchorse.mclib.client.InputRenderer;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.mclib.GuiAbstractDashboard;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanels;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.client.gui.utils.resizers.IResizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.function.Supplier;

/**
 * Camera editor GUI
 *
 * This GUI provides tools for managing camera profiles. 
 */
@SideOnly(Side.CLIENT)
public class GuiCameraDashboard extends GuiAbstractDashboard
{
    /**
     * An instance of a camera editor
     */
    public static GuiCameraDashboard cameraEditor;

    /**
     * Camera editor panel
     */
    public GuiCameraEditor camera;

    /**
     * Flight mode
     */
    public Flight flight;

    public GuiPlaybackScrub timeline;

    /**
     * Open the camera editor
     */
    public static GuiCameraDashboard openCameraEditor()
    {
        Minecraft mc = Minecraft.getMinecraft();
        GuiCameraDashboard editor = GuiCameraDashboard.getCameraEditor();

        mc.displayGuiScreen(editor);

        return editor;
    }

    /**
     * Get camera editor
     */
    public static GuiCameraDashboard getCameraEditor()
    {
        if (cameraEditor == null)
        {
            cameraEditor = new GuiCameraDashboard(Minecraft.getMinecraft());
        }

        return cameraEditor;
    }

    /**
     * Initialize the camera editor with a camera profile.
     */
    public GuiCameraDashboard(Minecraft mc)
    {
        super(mc);

        this.timeline = new GuiPlaybackScrub(mc, this.camera, null);
        this.timeline.flex().relative(this.panels.view).set(10, 0, 0, 20).y(1F).w(1, -20);
        this.panels.add(this.timeline);

        this.panels.view.flex().h.offset -= 20;

        /* Keybinds */
        IKey editor = IKey.lang("aperture.gui.editor.keys.editor.title");
        IKey modes = IKey.lang("aperture.gui.editor.keys.modes.title");
        Supplier<Boolean> active = this::isFlightDisabled;

        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.toggle"), Keyboard.KEY_F1, () -> this.panels.toggleVisible()).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.plause"), Keyboard.KEY_SPACE, () -> this.camera.plause.clickItself(GuiBase.getCurrent())).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.next_fixture"), Keyboard.KEY_RIGHT, this::jumpToNextFixture).held(Keyboard.KEY_LSHIFT).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.prev_fixture"), Keyboard.KEY_LEFT, this::jumpToPrevFixture).held(Keyboard.KEY_LSHIFT).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.next"), Keyboard.KEY_RIGHT, this::jumpToNextFrame).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.prev"), Keyboard.KEY_LEFT, this::jumpToPrevFrame).active(active).category(editor);

        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.flight"), Keyboard.KEY_F, () -> this.camera.cameraOptions.flight.clickItself(GuiBase.getCurrent())).category(modes);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.vertical"), Keyboard.KEY_V, () -> this.flight.toggleMovementType()).active(() -> this.flight.isFlightEnabled()).category(modes);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.sync"), Keyboard.KEY_S, () -> this.camera.cameraOptions.sync.clickItself(GuiBase.getCurrent())).active(active).category(modes);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.ouside"), Keyboard.KEY_O, () -> this.camera.cameraOptions.outside.clickItself(GuiBase.getCurrent())).active(active).category(modes);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.looping"), Keyboard.KEY_L, () -> this.camera.cameraOptions.loop.clickItself(GuiBase.getCurrent())).active(active).category(modes);
    }

    /**
     * Jump to the next camera fixture
     */
    public void jumpToNextFixture()
    {
        this.timeline.setValueFromScrub((int) this.camera.getProfile().calculateOffset(this.timeline.value, true));
    }

    /**
     * Jump to previous fixture
     */
    public void jumpToPrevFixture()
    {
        this.timeline.setValueFromScrub((int) this.camera.getProfile().calculateOffset(this.timeline.value - 1, false));
    }

    /**
     * Jump to the next frame (tick)
     */
    public void jumpToNextFrame()
    {
        this.timeline.setValueFromScrub(this.timeline.value + 1);
    }

    /**
     * Jump to the previous frame (tick)
     */
    public void jumpToPrevFrame()
    {
        this.timeline.setValueFromScrub(this.timeline.value - 1);
    }

    /**
     * This GUI shouldn't pause the game, because camera runs on the world's
     * update loop.
     */
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected GuiDashboardPanels createDashboard(Minecraft mc)
    {
        return new GuiCameraPanels(mc, this);
    }

    @Override
    protected void registerPanels(Minecraft mc)
    {
        /* Create flight mode */
        this.flight = new Flight(this);
        this.root.prepend(this.flight);

        /* Register panels */
        this.panels.registerPanel(this.camera = new GuiCameraEditor(mc, this, ClientProxy.runner), IKey.str("Camera editor"), APIcons.CAMERA);

        if (McLib.debugPanel.get())
        {
            this.panels.registerPanel(new GuiDebugPanel(mc, this), IKey.str("Debug panel"), Icons.POSE);
        }

        Aperture.EVENT_BUS.post(new CameraEditorDashboardEvent.RegisteringPanels(this));

        this.panels.setPanel(this.camera);
    }

    public boolean isFlightEnabled()
    {
        return this.flight.isFlightEnabled();
    }

    public boolean isFlightDisabled()
    {
        return !this.flight.isFlightEnabled();
    }

	public void exit()
    {
        this.closeScreen();
	}

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawOverlays(this.context);
        this.camera.updateLogic(this.context);

            super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.panels.canBeSeen())
        {
            this.drawInformation(this.context);
            this.camera.drawIcons(this.context);
        }
    }

    private void drawInformation(GuiContext context)
    {
        /* Display flight speed */
        GuiCameraEditor editor = this.camera;
        IResizer panel = this.camera.panel.resizer();

        if (this.flight.isFlightEnabled())
        {
            this.flight.drawSpeed(this.fontRenderer, panel.getX() + panel.getW() - 10, panel.getY() + panel.getH() - 5);
        }

        /* Display position variables */
        if ((editor.isSyncing() || editor.getRunner().isRunning()) && Aperture.editorDisplayPosition.get())
        {
            editor.drawPosition(panel);
        }
    }

    /**
     * Draw different camera type overlays (custom texture overlay, letterbox,
     * rule of thirds and crosshair)
     */
    private void drawOverlays(GuiContext context)
    {
        int sw = context.screen.width;
        int sh = context.screen.height;

        if (Aperture.editorOverlay.get() && this.camera.overlayLocation != null)
        {
            this.mc.renderEngine.bindTexture(this.camera.overlayLocation);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, sw, sh, sw, sh);
        }

        /* Readjustable values for rule of thirds in case of letter box enabled */
        int rx = 0;
        int ry = 0;
        int rw = sw;
        int rh = sh;

        if (Aperture.editorLetterbox.get() && this.camera.aspectRatio > 0)
        {
            int width = (int) (this.camera.aspectRatio * sh);

            if (width != sw)
            {
                if (width < sw)
                {
                    /* Horizontal bars */
                    int w = (sw - width) / 2;

                    Gui.drawRect(0, 0, w, sh, 0xff000000);
                    Gui.drawRect(sw - w, 0, sw, sh, 0xff000000);

                    rx = w;
                    rw -= w * 2;
                }
                else
                {
                    /* Vertical bars */
                    int h = (int) (sh - (1F / this.camera.aspectRatio * sw)) / 2;

                    Gui.drawRect(0, 0, sw, h, 0xff000000);
                    Gui.drawRect(0, sh - h, sw, sh, 0xff000000);

                    ry = h;
                    rh -= h * 2;
                }
            }
        }

        if (!this.panels.canBeSeen())
        {
            InputRenderer.disable();

            /* Little tip for users who don't know what they did */
            if (this.canRenderF1Tooltip())
            {
                this.fontRenderer.drawStringWithShadow(I18n.format("aperture.gui.editor.f1"), 5, context.screen.height - 12, 0xffffff);
            }

            return;
        }

        if (Aperture.editorRuleOfThirds.get())
        {
            int color = 0xcccc0000;

            Gui.drawRect(rx + rw / 3 - 1, ry, rx + rw / 3, ry + rh, color);
            Gui.drawRect(rx + rw - rw / 3, ry, rx + rw - rw / 3 + 1, ry + rh, color);

            Gui.drawRect(rx, ry + rh / 3 - 1, rx + rw, ry + rh / 3, color);
            Gui.drawRect(rx, ry + rh - rh / 3, rx + rw, ry + rh - rh / 3 + 1, color);
        }

        if (Aperture.editorCrosshair.get())
        {
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            this.mc.renderEngine.bindTexture(Gui.ICONS);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1, 1, 1, 1);
            this.drawTexturedModalRect(this.viewport.mx() - 7, this.viewport.my() - 7, 0, 0, 16, 16);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
    }

    /**
     * Whether the F1 tooltip (for users who accidentally hit F1 without knowing)
     * should be rendered?
     */
    private boolean canRenderF1Tooltip()
    {
        return Aperture.editorF1Tooltip.get() && !(this.camera.getRunner().isRunning() || this.camera.minema.isRecording());
    }
}