package mchorse.aperture.client;

import mchorse.aperture.Aperture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Mouse renderer
 * 
 * This class is responsible for rendering a mouse pointer on the screen 
 */
public class MouseRenderer
{
    public static final ResourceLocation MOUSE_POINTER = new ResourceLocation("aperture:textures/gui/mouse.png");

    @SubscribeEvent
    public void onDrawEvent(DrawScreenEvent.Post event)
    {
        if (!Aperture.proxy.config.gui_render_mouse)
        {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 1000);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        Minecraft.getMinecraft().renderEngine.bindTexture(MOUSE_POINTER);
        Gui.drawModalRectWithCustomSizedTexture(event.getMouseX(), event.getMouseY(), 0, 0, 16, 16, 16, 16);
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
    }
}