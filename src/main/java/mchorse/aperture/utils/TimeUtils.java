package mchorse.aperture.utils;

import mchorse.aperture.Aperture;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

public class TimeUtils
{
    public static String formatTime(long ticks)
    {
        if (Aperture.editorSeconds.get())
        {
            long seconds = (long) (ticks / 20D);
            int milliseconds = (int) (ticks % 20 == 0 ? 0 : ticks % 20 * 5D);

            return seconds + "." + StringUtils.leftPad(String.valueOf(milliseconds), 2, "0");
        }

        return String.valueOf(ticks);
    }

    public static double toTime(long ticks)
    {
        return Aperture.editorSeconds.get() ? ticks / 20D : ticks;
    }

    public static long fromTime(double time)
    {
        return Aperture.editorSeconds.get() ? Math.round(time * 20D) : (long) time;
    }

    @SideOnly(Side.CLIENT)
    public static void configure(GuiTrackpadElement element, long defaultValue)
    {
        if (Aperture.editorSeconds.get())
        {
            element.values(0.1D, 0.05D, 0.25D).limit(defaultValue / 20D, Double.POSITIVE_INFINITY, false);
        }
        else
        {
            element.values(1.0D).limit(defaultValue, Double.POSITIVE_INFINITY, true);
        }
    }
}