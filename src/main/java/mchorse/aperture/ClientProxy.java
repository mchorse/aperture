package mchorse.aperture;

import java.io.File;

import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraRenderer;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.FollowFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.modifiers.AngleModifier;
import mchorse.aperture.camera.modifiers.FollowModifier;
import mchorse.aperture.camera.modifiers.LookModifier;
import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.camera.modifiers.OrbitModifier;
import mchorse.aperture.camera.modifiers.ShakeModifier;
import mchorse.aperture.camera.modifiers.TranslateModifier;
import mchorse.aperture.client.KeyboardHandler;
import mchorse.aperture.client.MouseRenderer;
import mchorse.aperture.client.RenderingHandler;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.GuiCircularFixturePanel;
import mchorse.aperture.client.gui.panels.GuiFollowFixturePanel;
import mchorse.aperture.client.gui.panels.GuiIdleFixturePanel;
import mchorse.aperture.client.gui.panels.GuiLookFixturePanel;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiAngleModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiFollowModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiLookModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiMathModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiOrbitModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiShakeModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiTranslateModifierPanel;
import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.commands.CommandLoadChunks;
import mchorse.aperture.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
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
        /* Register camera fixtures */
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        GuiCameraEditor.PANELS.put(IdleFixture.class, new GuiIdleFixturePanel(font));
        GuiCameraEditor.PANELS.put(PathFixture.class, new GuiPathFixturePanel(font));
        GuiCameraEditor.PANELS.put(LookFixture.class, new GuiLookFixturePanel(font));
        GuiCameraEditor.PANELS.put(FollowFixture.class, new GuiFollowFixturePanel(font));
        GuiCameraEditor.PANELS.put(CircularFixture.class, new GuiCircularFixturePanel(font));

        FixtureRegistry.registerClient(IdleFixture.class, I18n.format("aperture.gui.fixtures.idle"), new Color(0.085F, 0.62F, 0.395F));
        FixtureRegistry.registerClient(PathFixture.class, I18n.format("aperture.gui.fixtures.path"), new Color(0.408F, 0.128F, 0.681F));
        FixtureRegistry.registerClient(LookFixture.class, I18n.format("aperture.gui.fixtures.look"), new Color(0.298F, 0.690F, 0.972F));
        FixtureRegistry.registerClient(FollowFixture.class, I18n.format("aperture.gui.fixtures.follow"), new Color(0.85F, 0.137F, 0.329F));
        FixtureRegistry.registerClient(CircularFixture.class, I18n.format("aperture.gui.fixtures.circular"), new Color(0.298F, 0.631F, 0.247F));

        /* Register camera modifiers */
        GuiModifiersManager.PANELS.put(ShakeModifier.class, GuiShakeModifierPanel.class);
        GuiModifiersManager.PANELS.put(MathModifier.class, GuiMathModifierPanel.class);
        GuiModifiersManager.PANELS.put(LookModifier.class, GuiLookModifierPanel.class);
        GuiModifiersManager.PANELS.put(FollowModifier.class, GuiFollowModifierPanel.class);
        GuiModifiersManager.PANELS.put(TranslateModifier.class, GuiTranslateModifierPanel.class);
        GuiModifiersManager.PANELS.put(AngleModifier.class, GuiAngleModifierPanel.class);
        GuiModifiersManager.PANELS.put(OrbitModifier.class, GuiOrbitModifierPanel.class);

        ModifierRegistry.registerClient(ShakeModifier.class, I18n.format("aperture.gui.modifiers.shake"), new Color(0.085F, 0.62F, 0.395F));
        ModifierRegistry.registerClient(MathModifier.class, I18n.format("aperture.gui.modifiers.math"), new Color(0.408F, 0.128F, 0.681F));
        ModifierRegistry.registerClient(LookModifier.class, I18n.format("aperture.gui.modifiers.look"), new Color(0.298F, 0.690F, 0.972F));
        ModifierRegistry.registerClient(FollowModifier.class, I18n.format("aperture.gui.modifiers.follow"), new Color(0.85F, 0.137F, 0.329F));
        ModifierRegistry.registerClient(TranslateModifier.class, I18n.format("aperture.gui.modifiers.translate"), new Color(0.298F, 0.631F, 0.247F));
        ModifierRegistry.registerClient(AngleModifier.class, I18n.format("aperture.gui.modifiers.angle"), new Color(0.847F, 0.482F, 0.043F));
        ModifierRegistry.registerClient(OrbitModifier.class, I18n.format("aperture.gui.modifiers.orbit"), new Color(0.874F, 0.184F, 0.625F));

        cameraEditor = new GuiCameraEditor(runner);

        super.load(event);

        /* Event listeners */
        MinecraftForge.EVENT_BUS.register(new MouseRenderer());
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