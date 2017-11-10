package mchorse.aperture.client;

import java.util.List;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
     * On in game chat rendering
     *
     * This event handler disables chat rendering in camera editor menu.
     */
    @SubscribeEvent
    public void onChatDraw(RenderGameOverlayEvent.Chat event)
    {
        if (this.mc.currentScreen instanceof GuiCameraEditor)
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
}