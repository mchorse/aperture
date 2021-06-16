package mchorse.aperture.client;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiManualFixturePanel;
import mchorse.mclib.events.RenderOverlayEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * Rendering handler
 * 
 * This class is responsible for doing some pretty neat stuff related to 
 * rendering. 
 */
public class RenderingHandler
{
    private Minecraft mc = Minecraft.getMinecraft();

    /**
     * Renders recording overlay during HUD rendering
     */
    @SubscribeEvent
    public void onHUDRender(RenderGameOverlayEvent.Post event)
    {
        ScaledResolution resolution = event.getResolution();

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
        {
            GuiManualFixturePanel.drawHUD(resolution.getScaledWidth(), resolution.getScaledHeight());
        }
    }

    /**
     * On in game chat rendering
     *
     * This event handler disables chat rendering in camera editor menu.
     */
    @SubscribeEvent
    public void onChatDraw(RenderGameOverlayEvent.Chat event)
    {
        if (Aperture.editorHideChat.get() && this.mc.currentScreen instanceof GuiCameraEditor)
        {
            event.setCanceled(true);
        }
    }

    /**
     * Add Aperture debug strings, such as current tick of camera playback
     */
    @SubscribeEvent
    public void onHUDRender(RenderGameOverlayEvent.Text event)
    {
        if (this.mc.currentScreen instanceof GuiCameraEditor)
        {
            event.setCanceled(true);
        }

        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo)
        {
            return;
        }

        List<String> list = event.getLeft();

        if (ClientProxy.runner.isRunning())
        {
            list.add("Camera ticks " + ClientProxy.runner.ticks);
        }
    }

    /**
     * Render letter box outside of camera editor GUI
     */
    @SubscribeEvent
    public void onPreRenderOverlay(RenderOverlayEvent.Pre event)
    {
        if (!Aperture.editorLetterbox.get())
        {
            return;
        }

        GuiScreen screen = event.mc.currentScreen;
        boolean isCameraEditor = screen instanceof GuiCameraEditor;

        if (!(isCameraEditor || ClientProxy.runner.isRunning()))
        {
            return;
        }

        int screenW = event.resolution.getScaledWidth();
        int screenH = event.resolution.getScaledHeight();
        float aspectRatio = 16F / 9F;

        if (isCameraEditor)
        {
            aspectRatio = ((GuiCameraEditor) screen).aspectRatio;
        }
        else
        {
            aspectRatio = CameraUtils.parseAspectRation(Aperture.editorLetterboxAspect.get(), aspectRatio);
        }

        if (aspectRatio > 0)
        {
            int width = (int) (aspectRatio * screenH);

            if (width != screenW)
            {
                if (width < screenW)
                {
                    /* Horizontal bars */
                    int w = (screenW - width) / 2;

                    Gui.drawRect(0, 0, w, screenH, 0xff000000);
                    Gui.drawRect(screenW - w, 0, screenW, screenH, 0xff000000);
                }
                else
                {
                    /* Vertical bars */
                    int h = (int) (screenH - (1F / aspectRatio * screenW)) / 2;

                    Gui.drawRect(0, 0, screenW, h, 0xff000000);
                    Gui.drawRect(0, screenH - h, screenW, screenH, 0xff000000);
                }
            }
        }
    }
    
    /**
     * Disable FOV Modifier
     */
    @SubscribeEvent
    public void onFOVModifier(FOVModifier event)
    {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiCameraEditor)
        {
            event.setFOV(mc.gameSettings.fovSetting);
        }
    }
}