package mchorse.aperture.camera.curves;

import mchorse.aperture.utils.OptifineHelper;
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

        if (OptifineHelper.isFogFancy())
        {
            far *= 0.95F;
        }

        if (OptifineHelper.isFogFast())
        {
            far *= 0.83F;
        }

        if (far < 173F)
        {
            far = 173F;
        }

        double depth = ((far + near) * value - 2 * near * far) / value / (far - near) * 0.5 + 0.5;

        super.apply(depth);
    }
}
