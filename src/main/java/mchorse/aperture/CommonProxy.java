package mchorse.aperture;

import java.io.File;

import mchorse.aperture.capabilities.CapabilityHandler;
import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.CameraStorage;
import mchorse.aperture.config.ApertureConfig;
import mchorse.aperture.network.Dispatcher;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Common proxy
 *
 * This class is responsible for registering items, blocks, entities,
 * capabilities and event listeners on both sides (that's why it's a common
 * proxy).
 */
public class CommonProxy
{
    /**
     * Config
     */
    public ApertureConfig config;

    /**
     * Forge config
     */
    public Configuration forge;

    /**
     * Registers network messages (and their handlers), items, blocks, director
     * block tile entities and actor entity.
     */
    public void preLoad(FMLPreInitializationEvent event)
    {
        Dispatcher.register();

        /* Configuration */
        File config = new File(event.getModConfigurationDirectory(), "aperture/config.cfg");

        this.forge = new Configuration(config);
        this.config = new ApertureConfig(this.forge);

        MinecraftForge.EVENT_BUS.register(this.config);

        /* Capabilities */
        CapabilityManager.INSTANCE.register(ICamera.class, new CameraStorage(), Camera.class);
    }

    /**
     * This method is responsible for registering Mocap's event handler which
     * is responsible for capturing <s>pokemons</s> player actions.
     */
    public void load(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
    }

    /**
     * Triggered when config is changed
     */
    public void onConfigChange(Configuration config)
    {}
}