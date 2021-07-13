package mchorse.aperture.camera.curves;

import mchorse.aperture.client.AsmShaderHandler;

public class ShaderUniform1iCurve extends ShaderUniform1fCurve
{
    public ShaderUniform1iCurve(String name)
    {
        super(name);
    }

    @Override
    public void apply(double value)
    {
        AsmShaderHandler.uniform1i.put(this.name, (int) value);
    }

    @Override
    public void reset()
    {
        AsmShaderHandler.uniform1i.remove(this.name);
    }
}
