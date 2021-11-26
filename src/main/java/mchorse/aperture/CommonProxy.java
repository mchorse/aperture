package mchorse.aperture;

import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.DollyFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.ManualFixture;
import mchorse.aperture.camera.fixtures.NullFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.modifiers.AngleModifier;
import mchorse.aperture.camera.modifiers.DollyZoomModifier;
import mchorse.aperture.camera.modifiers.DragModifier;
import mchorse.aperture.camera.modifiers.FollowModifier;
import mchorse.aperture.camera.modifiers.LookModifier;
import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.camera.modifiers.OrbitModifier;
import mchorse.aperture.camera.modifiers.RemapperModifier;
import mchorse.aperture.camera.modifiers.ShakeModifier;
import mchorse.aperture.camera.modifiers.TranslateModifier;
import mchorse.aperture.capabilities.CapabilityHandler;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.CameraStorage;
import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.network.Dispatcher;
import mchorse.mclib.config.ConfigBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Common proxy
 *
 * This class is responsible for configuration, capabilities, event listeners 
 * and packet dispatcher.
 */
public class CommonProxy
{
   /**
     * Registers network messages (and their handlers), items, blocks, director
     * block tile entities and actor entity.
     */
    public void preLoad(FMLPreInitializationEvent event)
    {
        Dispatcher.register();

        /* Capabilities */
        CapabilityManager.INSTANCE.register(ICamera.class, new CameraStorage(), Camera::new);

        /* Register camera fixtures and modifiers */
        FixtureRegistry.register("idle", IdleFixture.class);
        FixtureRegistry.register("dolly", DollyFixture.class);
        FixtureRegistry.register("circular", CircularFixture.class);
        FixtureRegistry.register("path", PathFixture.class);
        FixtureRegistry.register("keyframe", KeyframeFixture.class);
        FixtureRegistry.register("null", NullFixture.class);
        FixtureRegistry.register("manual", ManualFixture.class);

        ModifierRegistry.register("angle", AngleModifier.class);
        ModifierRegistry.register("translate", TranslateModifier.class);
        ModifierRegistry.register("shake", ShakeModifier.class);
        ModifierRegistry.register("drag", DragModifier.class);
        ModifierRegistry.register("look", LookModifier.class);
        ModifierRegistry.register("follow", FollowModifier.class);
        ModifierRegistry.register("orbit", OrbitModifier.class);
        ModifierRegistry.register("math", MathModifier.class);
        ModifierRegistry.register("remapper", RemapperModifier.class);
        ModifierRegistry.register("dolly_zoom", DollyZoomModifier.class);
    }

    /**
     * This method is responsible for registering Mocap's event handler 
     * which is responsible for capturing <s>pokemons</s> player actions.
     */
    public void load(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
    }

    /**
     * Get language string
     */
    public String getLanguageString(String key, String defaultComment)
    {
        return defaultComment;
    }
    
    /**
     * Register client only configuration
     */
    public void registerClientConfig(ConfigBuilder builder)
    {
    }
}