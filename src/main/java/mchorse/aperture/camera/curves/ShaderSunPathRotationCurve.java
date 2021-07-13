package mchorse.aperture.camera.curves;

import mchorse.aperture.client.AsmShaderHandler;
import net.minecraft.client.resources.I18n;
import net.optifine.shaders.Shaders;

public class ShaderSunPathRotationCurve extends AbstractCurve
{
    @Override
    public String getTranslatedName()
    {
        return I18n.format("aperture.gui.curves.shader.sun_path_rotation");
    }

    @Override
    public void apply(double value)
    {
        Shaders.sunPathRotation = (float) value;
    }

    @Override
    public void reset()
    {
        Shaders.sunPathRotation = AsmShaderHandler.sunPathRotation;
    }

}
