package mchorse.aperture;

import java.io.File;

import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraRenderer;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.client.KeyboardHandler;
import mchorse.aperture.client.RenderingHandler;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.commands.CommandLoadChunks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client proxy
 *
 * This class is responsible for registering client side event handlers, client 
 * commands, storing camera related stuff, and "proxify" everything related to 
 * client side.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    /* Camera stuff */
    public static CameraRenderer renderer = new CameraRenderer();
    public static CameraControl control = new CameraControl();
    public static CameraRunner runner;

    public static GuiCameraEditor cameraEditor;
    public static KeyboardHandler keys;

    /**
     * Event bus for handling camera editor events (I don't want to spam a lot 
     * of events in the main event bus). 
     */
    public static EventBus EVENT_BUS = new EventBus();

    /* Files */
    public static File config;
    public static File cameras;

    /**
     * Get client cameras storage location
     * 
     * This method returns File pointer to a folder where client side camera 
     * profiles are stored. This method will return {@code null} if it was 
     * invoked when the game is in the main menu.
     */
    public static File getClientCameras()
    {
        Minecraft mc = Minecraft.getMinecraft();
        ServerData data = mc.getCurrentServerData();

        File file = null;

        if (data != null)
        {
            /* Removing port, because this will distort the folder name */
            file = new File(cameras, data.serverIP.replaceAll(":[\\w]{1,5}$", ""));
        }
        else if (mc.isSingleplayer())
        {
            file = new File(cameras, mc.getIntegratedServer().getWorldName());
        }

        if (file != null)
        {
            file.mkdirs();

            return file;
        }

        return null;
    }

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

        runner = new CameraRunner();
    }

    /**
     * Subscribe all event listeners to EVENT_BUS and attach any client-side
     * commands to the ClientCommandRegistry.
     */
    @Override
    public void load(FMLInitializationEvent event)
    {
        cameraEditor = new GuiCameraEditor(runner);

        super.load(event);

        /* Event listeners */
        MinecraftForge.EVENT_BUS.register(new RenderingHandler());
        MinecraftForge.EVENT_BUS.register(keys = new KeyboardHandler());
        MinecraftForge.EVENT_BUS.register(renderer);

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

        renderer.roll.friction = config.getFloat("roll_friction", smooth, 0.985F, 0.0F, 0.99999F, "Roll acceleration friction (how fast it slows down)", prefix + "roll_friction");
        renderer.fov.friction = config.getFloat("fov_friction", smooth, 0.985F, 0.0F, 0.99999F, "FOV acceleration friction (how fast it slows down)", prefix + "fov_friction");

        renderer.roll.factor = config.getFloat("roll_speed", smooth, 0.01F, 0.0F, 10.0F, "Roll acceleration speed", prefix + "roll_speed");
        renderer.fov.factor = config.getFloat("fov_speed", smooth, 0.075F, 0.0F, 10.0F, "FOV acceleration speed", prefix + "fov_speed");

        renderer.smooth.enabled = config.getBoolean("smooth_enabled", smooth, false, "Enable smooth camera", prefix + "smooth_enabled");
        renderer.smooth.fricX = config.getFloat("mouse_x_friction", smooth, 0.92F, 0.0F, 1.0F, "Smooth mouse X friction", prefix + "mouse_x_friction");
        renderer.smooth.fricY = config.getFloat("mouse_y_friction", smooth, 0.92F, 0.0F, 1.0F, "Smooth mouse Y friction", prefix + "mouse_y_friction");
    }
}