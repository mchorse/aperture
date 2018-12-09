package mchorse.aperture.client.gui.utils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * GUI utilities
 */
public class GuiUtils
{
    /**
     * Draw right aligned string with shadow
     */
    public static void drawRightString(FontRenderer font, String str, int x, int y, int color)
    {
        int w = font.getStringWidth(str);

        Gui.drawRect(x - w - 2, y - 1, x + 2, y + font.FONT_HEIGHT + 1, 0xaa000000);
        font.drawStringWithShadow(str, x - w, y, color);
    }

    public static void drawOutline(int x, int y, int w, int h, int color)
    {
        Gui.drawRect(x, y, x + w, y + 1, color);
        Gui.drawRect(x, y + 19, x + w, y + 20, color);
        Gui.drawRect(x, y, x + 1, y + h, color);
        Gui.drawRect(x + w, y, x + w + 1, y + h, color);
    }
}