package mchorse.aperture.core;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class APCoreMod implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] {APCoreClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass()
    {
        return APCoreModInfo.class.getName();
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {}

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
