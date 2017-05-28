package mchorse.aperture;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Aperture camera mod
 */
@Mod(modid = Aperture.MODID, name = Aperture.MODNAME, version = Aperture.VERSION, guiFactory = Aperture.GUI_FACTORY)
public class Aperture
{
    /* Mod info */
    public static final String MODID = "aperture";
    public static final String MODNAME = "Aperture";
    public static final String VERSION = "1.0";
    public static final String GUI_FACTORY = "mchorse.aperture.config.gui.GuiFactory";

    /* Proxies */
    public static final String CLIENT_PROXY = "mchorse.aperture.ClientProxy";
    public static final String SERVER_PROXY = "mchorse.aperture.CommonProxy";

    /* Forge stuff */
    @Mod.Instance
    public static Aperture instance;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static CommonProxy proxy;

    @EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        proxy.preLoad(event);
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.load(event);
    }
}