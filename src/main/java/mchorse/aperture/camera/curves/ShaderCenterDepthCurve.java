package mchorse.aperture.camera.curves;

import net.minecraft.client.Minecraft;

public class ShaderCenterDepthCurve extends ShaderUniform1fCurve
{
    public ShaderCenterDepthCurve()
    {
        super("centerDepthSmooth");
    }

    @Override
    public void apply(double value)
    {
        double near = 0.05;
        double far = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 32;
        
        if (far < 173)
        {
            far = 173;
        }
        
        double depth = ((far + near) * value - 2 * near * far) / value / (far - near) * 0.5 + 0.5;
        
        super.apply(depth);
    }
}
