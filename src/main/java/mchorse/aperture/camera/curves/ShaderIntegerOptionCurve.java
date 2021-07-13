package mchorse.aperture.camera.curves;

import mchorse.aperture.client.AsmShaderHandler;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;

public class ShaderIntegerOptionCurve extends ShaderUniform1iCurve
{
    public final String id;
    
    public ShaderIntegerOptionCurve(String id)
    {
        super(AsmShaderHandler.uniformPrefix + id);
        this.id = id;
    }

    @Override
    public String getTranslatedName()
    {
        ShaderOption option = Shaders.getShaderOption(this.id);

        if (option != null && !option.getName().equals(option.getNameText()))
        {
            return option.getNameText() + "/" + this.id;
        }

        return this.id;
    }
}
