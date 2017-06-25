package mchorse.aperture.client.gui.utils;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;

/**
 * GUI utilities
 */
public class GuiUtils
{
    /**
     * Draw an entity on the screen.
     *
     * Taken <s>stolen</s> from minecraft's class GuiInventory. I wonder what's
     * the license of minecraft's decompiled code?
     */
    public static void drawEntityOnScreen(int posX, int posY, int scale, int mouseX, int mouseY, EntityLivingBase ent)
    {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 100.0F);
        GlStateManager.scale((-scale), scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;

        ent.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
        ent.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
        ent.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;

        GlStateManager.translate(0.0F, 0.0F, 0.0F);

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.doRenderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);

        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    /**
     * Draws a rectangle with a horizontal gradient between with specified
     * colors, the code is borrowed form {@link #drawGradientRect(int, int, int, int, int, int)}
     */
    public static void drawHorizontalGradientRect(int left, int top, int right, int bottom, int startColor, int endColor, float zLevel)
    {
        float a1 = (startColor >> 24 & 255) / 255.0F;
        float r1 = (startColor >> 16 & 255) / 255.0F;
        float g1 = (startColor >> 8 & 255) / 255.0F;
        float b1 = (startColor & 255) / 255.0F;
        float a2 = (endColor >> 24 & 255) / 255.0F;
        float r2 = (endColor >> 16 & 255) / 255.0F;
        float g2 = (endColor >> 8 & 255) / 255.0F;
        float b2 = (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(right, top, zLevel).color(r2, g2, b2, a2).endVertex();
        vertexbuffer.pos(left, top, zLevel).color(r1, g1, b1, a1).endVertex();
        vertexbuffer.pos(left, bottom, zLevel).color(r1, g1, b1, a1).endVertex();
        vertexbuffer.pos(right, bottom, zLevel).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /**
     * Draw right aligned string with shadow
     */
    public static void drawRightString(FontRenderer font, String str, int x, int y, int color)
    {
        int w = font.getStringWidth(str);

        Gui.drawRect(x - w - 2, y - 1, x + 2, y + font.FONT_HEIGHT + 1, 0xaa000000);
        font.drawStringWithShadow(str, x - w, y, color);
    }

    /**
     * Scissor (clip) the screen 
     */
    public static void scissor(int x, int y, int w, int h, int sw, int sh)
    {
        Minecraft mc = Minecraft.getMinecraft();

        /* F*$! those ints */
        float rx = (float) Math.ceil((double) mc.displayWidth / (double) sw);
        float ry = (float) Math.ceil((double) mc.displayHeight / (double) sh);

        /* Clipping area around scroll area */
        int xx = (int) (x * rx);
        int yy = (int) (mc.displayHeight - (y + h) * ry);
        int ww = (int) (w * rx);
        int hh = (int) (h * ry);

        GL11.glScissor(xx, yy, ww, hh);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public static void drawOutline(int x, int y, int w, int h, int color)
    {
        Gui.drawRect(x, y, x + w, y + 1, color);
        Gui.drawRect(x, y + 19, x + w, y + 20, color);
        Gui.drawRect(x, y, x + 1, y + h, color);
        Gui.drawRect(x + w, y, x + w + 1, y + h, color);
    }

    public static void setSize(GuiButton button, int x, int y, int w, int h)
    {
        button.xPosition = x;
        button.yPosition = y;
        button.width = w;
        button.height = h;
    }

    public static void setSize(GuiTextField field, int x, int y, int w, int h)
    {
        field.xPosition = x + 1;
        field.yPosition = y + 1;
        field.width = w - 2;
        field.height = h - 2;
    }
}