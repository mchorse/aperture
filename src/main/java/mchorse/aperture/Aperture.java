package mchorse.aperture;

import org.apache.logging.log4j.Logger;

import mchorse.aperture.commands.CommandAperture;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * Main entry point of Aperture
 *
 * This mod allows people to create Minecraft cinematics. It provides tools for
 * managing camera profiles and camera fixtures withing camera profiles via
 * command line (chat commands) or GUI (camera editor).
 *
 * This mod provides a lot of tools related to camera.
 */
@Mod(modid = Aperture.MODID, name = Aperture.MODNAME, version = Aperture.VERSION, guiFactory = Aperture.GUI_FACTORY, dependencies = "required-after:mclib@[%MCLIB%,)", updateJSON = "https://raw.githubusercontent.com/mchorse/aperture/master/version.json")
public class Aperture
{
    /* Mod info */
    public static final String MODID = "aperture";
    public static final String MODNAME = "Aperture";
    public static final String VERSION = "%VERSION%";
    public static final String GUI_FACTORY = "mchorse.aperture.config.gui.GuiFactory";

    /* Proxies */
    public static final String CLIENT_PROXY = "mchorse.aperture.ClientProxy";
    public static final String SERVER_PROXY = "mchorse.aperture.CommonProxy";

    /* Forge stuff */
    @Mod.Instance
    public static Aperture instance;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static CommonProxy proxy;

    /* Mod's logger */
    public static Logger LOGGER;

    @EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();

        proxy.preLoad(event);
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.load(event);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandAperture());
    }
}
