package mchorse.aperture.camera.curves;

import mchorse.aperture.client.AsmShaderHandler;
import net.minecraft.client.resources.I18n;

public class ShaderUniform1fCurve extends AbstractCurve
{
    public final String name;
    
    public ShaderUniform1fCurve(String name)
    {
        this.name = name;
    }
    
    @Override
    public String getTranslatedName()
    {
        return I18n.format("aperture.gui.curves.shader." + this.convertTranslateKey(this.name));
    }

    @Override
    public void apply(double value)
    {
        AsmShaderHandler.uniform1f.put(this.name, (float) value);
    }

    @Override
    public void reset()
    {
        AsmShaderHandler.uniform1f.remove(this.name);
    }
}
