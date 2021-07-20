package mchorse.aperture.camera.curves;

import mchorse.aperture.client.AsmShaderHandler;
import net.minecraft.client.resources.I18n;
import net.optifine.shaders.Shaders;

public class ShaderSunPathRotationCurve extends ShaderUniform1fCurve
{
    public ShaderSunPathRotationCurve()
    {
        super("sunPathRotation");
    }

    @Override
    public void apply(double value)
    {
        super.apply(value);
        Shaders.sunPathRotation = (float) value;
    }

    @Override
    public void reset()
    {
        super.reset();
        Shaders.sunPathRotation = AsmShaderHandler.sunPathRotation;
    }

}
