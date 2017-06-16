package mchorse.aperture;

import java.io.File;

import mchorse.aperture.camera.CameraRenderer;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.client.KeyboardHandler;
import mchorse.aperture.client.RenderingHandler;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.commands.CommandLoadChunks;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client proxy
 *
 * This class is responsible for registering item models, block models, entity
 * renders and injecting actor skin resource pack.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static CameraRunner profileRunner = new CameraRunner();
    public static CameraRenderer profileRenderer = new CameraRenderer();
    public static GuiCameraEditor cameraEditor;
    public static KeyboardHandler keys;
    public static File config;
    public static File cameras;

    /**
     * Register mod items, blocks, tile entites and entities, load item,
     * block models and register entity renderer.
     */
    @Override
    public void preLoad(FMLPreInitializationEvent event)
    {
        String path = event.getSuggestedConfigurationFile().getAbsolutePath();
        path = path.substring(0, path.length() - 4);

        config = new File(path);
        cameras = new File(path + "/aperture/cameras/");

        super.preLoad(event);
    }

    /**
     * Subscribe all event listeners to EVENT_BUS and attach any client-side
     * commands to the ClientCommandRegistry.
     */
    @Override
    public void load(FMLInitializationEvent event)
    {
        cameraEditor = new GuiCameraEditor(profileRunner);

        super.load(event);

        /* Event listeners */
        MinecraftForge.EVENT_BUS.register(new RenderingHandler());
        MinecraftForge.EVENT_BUS.register(keys = new KeyboardHandler());
        MinecraftForge.EVENT_BUS.register(profileRenderer);

        /* Client commands */
        ClientCommandHandler.instance.registerCommand(new CommandCamera());
        ClientCommandHandler.instance.registerCommand(new CommandLoadChunks());
    }

    /**
     * Applies client side options
     */
    @Override
    public void onConfigChange(Configuration config)
    {
        String smooth = "smooth";
        String prefix = "aperture.config.smooth.";

        profileRenderer.roll.friction = config.getFloat("roll_friction", smooth, 0.985F, 0.0F, 0.99999F, "Roll acceleration friction (how fast it slows down)", prefix + "roll_friction");
        profileRenderer.fov.friction = config.getFloat("fov_friction", smooth, 0.985F, 0.0F, 0.99999F, "FOV acceleration friction (how fast it slows down)", prefix + "fov_friction");

        profileRenderer.roll.factor = config.getFloat("roll_speed", smooth, 0.01F, 0.0F, 10.0F, "Roll acceleration speed", prefix + "roll_speed");
        profileRenderer.fov.factor = config.getFloat("fov_speed", smooth, 0.075F, 0.0F, 10.0F, "FOV acceleration speed", prefix + "fov_speed");

        profileRenderer.smooth.enabled = config.getBoolean("smooth_enabled", smooth, false, "Enable smooth camera", prefix + "smooth_enabled");
        profileRenderer.smooth.fricX = config.getFloat("mouse_x_friction", smooth, 0.92F, 0.0F, 1.0F, "Smooth mouse X friction", prefix + "mouse_x_friction");
        profileRenderer.smooth.fricY = config.getFloat("mouse_y_friction", smooth, 0.92F, 0.0F, 1.0F, "Smooth mouse Y friction", prefix + "mouse_y_friction");
    }
}