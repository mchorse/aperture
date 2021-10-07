package mchorse.aperture.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.Shaders;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
public class OptifineHelper
{
    public static final boolean shaderpackSupported;
    public static final Method isFogFast;
    public static final Method isFogFancy;

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

        boolean supported = false;
        Method fast = null;
        Method fancy = null;

        try
        {
            Class<?> config = Class.forName("Config");

            fast = config.getMethod("isFogFast");
            fancy = config.getMethod("isFogFancy");

            Class.forName("net.optifine.shaders.Shaders");

            supported = true;
        }
        catch (ClassNotFoundException | SecurityException | IllegalArgumentException | NoSuchMethodException e)
        {}

        shaderpackSupported = supported;
        isFogFast = fast;
        isFogFancy = fancy;
    }

    public static boolean isZooming()
    {
        if (ofCameraZoom != null)
        {
            return getKeybind().isKeyDown();
        }

        return false;
    }
    
    public static boolean isShaderLoaded()
    {
        if (shaderpackSupported)
        {
            return Shaders.shaderPackLoaded;
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

    public static boolean isFogFast()
    {
        if (isFogFast != null)
        {
            try
            {
                return (boolean) isFogFast.invoke(null);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {}
        }

        return false;
    }

    public static boolean isFogFancy()
    {
        if (isFogFancy != null)
        {
            try
            {
                return (boolean) isFogFancy.invoke(null);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {}
        }

        return false;
    }
}