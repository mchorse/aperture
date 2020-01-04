package mchorse.aperture.client;

import mchorse.aperture.Aperture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

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
        if (!shouldRenderMouse())
        {
            return;
        }

        int x = event.getMouseX();
        int y = event.getMouseY();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 1000);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        Minecraft.getMinecraft().renderEngine.bindTexture(MOUSE_POINTER);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

        boolean left = Mouse.isButtonDown(0);
        boolean right = Mouse.isButtonDown(1);
        boolean middle = Mouse.isButtonDown(2);

        if (left || right || middle)
        {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            x += 16;
            y += 2;

            /* Outline */
            Gui.drawRect(x - 1, y, x + 13, y + 16, 0xff000000);
            Gui.drawRect(x, y - 1, x + 12, y + 17, 0xff000000);
            /* Background */
            Gui.drawRect(x, y + 1, x + 12, y + 15, 0xffffffff);
            Gui.drawRect(x + 1, y, x + 11, y + 1, 0xffffffff);
            Gui.drawRect(x + 1, y + 15, x + 11, y + 16, 0xffffffff);
            /* Over outline */
            Gui.drawRect(x, y + 7, x + 12, y + 8, 0xffeeeeee);

            if (left)
            {
                Gui.drawRect(x + 1, y, x + 6, y + 7, 0xffcccccc);
                Gui.drawRect(x, y + 1, x + 1, y + 7, 0xffaaaaaa);
            }

            if (right)
            {
                Gui.drawRect(x + 6, y, x + 11, y + 7, 0xffaaaaaa);
                Gui.drawRect(x + 11, y + 1, x + 12, y + 7, 0xff888888);
            }

            if (middle)
            {
                Gui.drawRect(x + 5, y + 1, x + 7, y + 5, 0xff444444);
                Gui.drawRect(x + 5, y + 4, x + 7, y + 5, 0xff333333);
            }
        }

        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
    }

    private static boolean shouldRenderMouse()
    {
        if (!Aperture.proxy.config.gui_render_mouse)
        {
            return false;
        }

        if (!Aperture.proxy.config.camera_editor_f1_tooltip && Minecraft.getMinecraft().currentScreen instanceof GuiCameraEditor && !((GuiCameraEditor) Minecraft.getMinecraft().currentScreen).elements.isVisible())
        {
            return false;
        }

        return true;
    }
}