package mchorse.aperture;

import java.io.File;
import java.util.HashMap;

import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraRenderer;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.FixtureRegistry.FixtureInfo;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.ModifierRegistry.ModifierInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.NullFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.modifiers.AngleModifier;
import mchorse.aperture.camera.modifiers.DragModifier;
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
import mchorse.aperture.client.gui.panels.GuiIdleFixturePanel;
import mchorse.aperture.client.gui.panels.GuiKeyframeFixturePanel;
import mchorse.aperture.client.gui.panels.GuiNullFixturePanel;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiAngleModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiDragModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiFollowModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiLookModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiMathModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiOrbitModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiShakeModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiTranslateModifierPanel;
import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.commands.CommandLoadChunks;
import mchorse.aperture.config.ApertureConfig;
import mchorse.aperture.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
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
    public static boolean server = false;

    /* Camera stuff */
    public static CameraRenderer renderer = new CameraRenderer();
    public static CameraControl control = new CameraControl();
    public static CameraRunner runner;

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
     * An instance of a camera editor
     */
    public static GuiCameraEditor cameraEditor;

    /**
     * Get camera editor
     */
    public static GuiCameraEditor getCameraEditor()
    {
        if (cameraEditor == null)
        {
            cameraEditor = new GuiCameraEditor(Minecraft.getMinecraft(), runner);
        }

        return cameraEditor;
    }

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
            file = new File(cameras, data.serverIP.replaceAll(":[\\w]{1,5}$", "").replaceAll("[^\\w\\d_\\- ]", "_"));
        }
        else if (mc.isSingleplayer())
        {
            /* I probably should've used getFolderName() in the beginning ... */
            file = new File(cameras, mc.getIntegratedServer().getWorldName().replaceAll("[^\\w\\d_\\- ]", "_"));
        }

        if (file != null)
        {
            file.mkdirs();
        }

        return file;
    }

    /**
     * Register mod items, blocks, tile entites and entities, load item,
     * block models and register entity renderer.
     */
    @Override
    public void preLoad(FMLPreInitializationEvent event)
    {
        FixtureRegistry.CLIENT = new HashMap<Class<? extends AbstractFixture>, FixtureInfo>();
        ModifierRegistry.CLIENT = new HashMap<Class<? extends AbstractModifier>, ModifierInfo>();

        config = new File(event.getModConfigurationDirectory(), "aperture");
        cameras = new File(config, "cameras");

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
        GuiCameraEditor.PANELS.put(IdleFixture.class, GuiIdleFixturePanel.class);
        GuiCameraEditor.PANELS.put(PathFixture.class, GuiPathFixturePanel.class);
        GuiCameraEditor.PANELS.put(CircularFixture.class, GuiCircularFixturePanel.class);
        GuiCameraEditor.PANELS.put(KeyframeFixture.class, GuiKeyframeFixturePanel.class);
        GuiCameraEditor.PANELS.put(NullFixture.class, GuiNullFixturePanel.class);

        FixtureRegistry.registerClient(IdleFixture.class, "aperture.gui.fixtures.idle", new Color(0.085F, 0.62F, 0.395F));
        FixtureRegistry.registerClient(PathFixture.class, "aperture.gui.fixtures.path", new Color(0.408F, 0.128F, 0.681F));
        FixtureRegistry.registerClient(CircularFixture.class, "aperture.gui.fixtures.circular", new Color(0.298F, 0.631F, 0.247F));
        FixtureRegistry.registerClient(KeyframeFixture.class, "aperture.gui.fixtures.keyframe", new Color(0.874F, 0.184F, 0.625F));
        FixtureRegistry.registerClient(NullFixture.class, "aperture.gui.fixtures.null", new Color(0.1F, 0.1F, 0.12F));

        /* Register camera modifiers */
        GuiModifiersManager.PANELS.put(ShakeModifier.class, GuiShakeModifierPanel.class);
        GuiModifiersManager.PANELS.put(MathModifier.class, GuiMathModifierPanel.class);
        GuiModifiersManager.PANELS.put(LookModifier.class, GuiLookModifierPanel.class);
        GuiModifiersManager.PANELS.put(FollowModifier.class, GuiFollowModifierPanel.class);
        GuiModifiersManager.PANELS.put(TranslateModifier.class, GuiTranslateModifierPanel.class);
        GuiModifiersManager.PANELS.put(AngleModifier.class, GuiAngleModifierPanel.class);
        GuiModifiersManager.PANELS.put(OrbitModifier.class, GuiOrbitModifierPanel.class);
        GuiModifiersManager.PANELS.put(DragModifier.class, GuiDragModifierPanel.class);

        ModifierRegistry.registerClient(ShakeModifier.class, "aperture.gui.modifiers.shake", new Color(0.085F, 0.62F, 0.395F));
        ModifierRegistry.registerClient(MathModifier.class, "aperture.gui.modifiers.math", new Color(0.408F, 0.128F, 0.681F));
        ModifierRegistry.registerClient(LookModifier.class, "aperture.gui.modifiers.look", new Color(0.298F, 0.690F, 0.972F));
        ModifierRegistry.registerClient(FollowModifier.class, "aperture.gui.modifiers.follow", new Color(0.85F, 0.137F, 0.329F));
        ModifierRegistry.registerClient(TranslateModifier.class, "aperture.gui.modifiers.translate", new Color(0.298F, 0.631F, 0.247F));
        ModifierRegistry.registerClient(AngleModifier.class, "aperture.gui.modifiers.angle", new Color(0.847F, 0.482F, 0.043F));
        ModifierRegistry.registerClient(OrbitModifier.class, "aperture.gui.modifiers.orbit", new Color(0.874F, 0.184F, 0.625F));
        ModifierRegistry.registerClient(DragModifier.class, "aperture.gui.modifiers.drag", new Color(0.1F, 0.5F, 1F));

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
    public void onConfigChange(ApertureConfig config)
    {
        String smooth = "smooth";

        renderer.roll.friction = config.getFloat("roll_friction", smooth, 0.985F, 0.0F, 0.99999F, "Roll acceleration friction (how fast it slows down)");
        renderer.fov.friction = config.getFloat("fov_friction", smooth, 0.985F, 0.0F, 0.99999F, "FOV acceleration friction (how fast it slows down)");

        renderer.roll.factor = config.getFloat("roll_speed", smooth, 0.01F, 0.0F, 10.0F, "Roll acceleration speed");
        renderer.fov.factor = config.getFloat("fov_speed", smooth, 0.075F, 0.0F, 10.0F, "FOV acceleration speed");

        renderer.smooth.enabled = config.getBoolean("smooth_enabled", smooth, false, "Enable smooth camera");
        renderer.smooth.fricX = config.getFloat("mouse_x_friction", smooth, 0.92F, 0.0F, 1.0F, "Smooth mouse X friction");
        renderer.smooth.fricY = config.getFloat("mouse_y_friction", smooth, 0.92F, 0.0F, 1.0F, "Smooth mouse Y friction");
    }

    /**
     * Client version of get language string.
     */
    @Override
    public String getLanguageString(String key, String defaultComment)
    {
        String comment = I18n.format(key);

        return comment.equals(key) ? defaultComment : comment;
    }
}