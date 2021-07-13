package mchorse.aperture.camera.curves;

import mchorse.aperture.client.AsmRenderingHandler;
import mchorse.aperture.client.AsmRenderingHandler.Curve;
import net.minecraft.client.resources.I18n;

public class VanillaAsmCurve extends AbstractCurve
{
    public final Curve curve;

    public VanillaAsmCurve(Curve curve)
    {
        this.curve = curve;
    }
    
    @Override
    public String getTranslatedName()
    {
        return I18n.format("aperture.gui.curves." + this.convertTranslateKey(this.curve.name()));
    }

    @Override
    public void apply(double value)
    {
        AsmRenderingHandler.values.put(this.curve, value);
    }

    @Override
    public void reset()
    {
        AsmRenderingHandler.values.remove(this.curve);
    }
}
