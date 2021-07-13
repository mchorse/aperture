package mchorse.aperture.core;

import net.minecraftforge.fml.common.DummyModContainer;

public class APCoreModInfo extends DummyModContainer
{
    @Override
    public String getName()
    {
        return "Aperture Core mod";
    }

    @Override
    public String getModId()
    {
        return "aperture_core";
    }

    @Override
    public Object getMod()
    {
        return null;
    }

    @Override
    public String getVersion()
    {
        return "%VERSION%";
    }
}
