package mchorse.aperture.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class OptifineHelper
{
    public static Field ofCameraZoom;
    public static KeyBinding ofCameraZoomKey;

    static
    {
        for (Field field : GameSettings.class.getDeclaredFields())
        {
            if (field.getName().startsWith("of") && field.getType().getName().contains("KeyBinding"))
            {
                ofCameraZoom = field;
                ofCameraZoom.setAccessible(true);

                break;
            }
        }
    }

    public static boolean isZooming()
    {
        if (ofCameraZoom != null)
        {
            return getKeybind().isKeyDown();
        }

        return false;
    }

    private static KeyBinding getKeybind()
    {
        if (ofCameraZoomKey == null)
        {
            try
            {
                ofCameraZoomKey = (KeyBinding) ofCameraZoom.get(Minecraft.getMinecraft().gameSettings);
            }
            catch (Exception e)
            {}
        }

        return ofCameraZoomKey;
    }
}