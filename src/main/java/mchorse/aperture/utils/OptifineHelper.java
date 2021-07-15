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
    public static Field ofDynamicFov;
    public static KeyBinding ofCameraZoomKey;

    static
    {
        for (Field field : GameSettings.class.getDeclaredFields())
        {
            if (field.getName().startsWith("of"))
            {
                if(field.getType().getName().contains("KeyBinding"))
                {
                    ofCameraZoom = field;
                    ofCameraZoom.setAccessible(true);
                }
                else if(field.getName().equals("ofDynamicFov"))
                {
                    ofDynamicFov = field;
                    ofDynamicFov.setAccessible(true);
                }
            }

            if (ofDynamicFov != null && ofCameraZoom != null) break;
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

    public static boolean dynamicFov()
    {
        try
        {
            return (boolean) ofDynamicFov.get(Minecraft.getMinecraft().gameSettings);
        }
        catch (Exception e)
        {}

        return true; //default minecraft value is true
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