package mchorse.aperture.utils;

public class OptifineShaderHelper
{
    public static final boolean shaderpackSupported;
    
    static {
        boolean supported = false;
        try
        {
            Class.forName("Config");
            Class.forName("net.optifine.shaders.Shaders");
            supported = true;
        }
        catch (ClassNotFoundException | SecurityException | IllegalArgumentException e)
        {}
        shaderpackSupported = supported;
    }
    
    
    
}
