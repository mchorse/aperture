package mchorse.aperture.camera.curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class BrightnessCurve extends AbstractCurve
{
    public float brightness = Minecraft.getMinecraft().gameSettings.gammaSetting;

    @Override
    public String getTranslatedName()
    {
        return I18n.format("options.gamma");
    }

    @Override
    public void apply(double value)
    {
        Minecraft.getMinecraft().gameSettings.gammaSetting = (float) value;
    }

    @Override
    public void reset()
    {
        Minecraft.getMinecraft().gameSettings.gammaSetting = this.brightness;
    }
}
