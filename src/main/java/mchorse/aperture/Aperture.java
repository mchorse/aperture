package mchorse.aperture;

import mchorse.aperture.commands.CommandAperture;
import mchorse.aperture.utils.OptifineHelper;
import mchorse.aperture.utils.mclib.ValueShaderOption;
import mchorse.mclib.McLib;
import mchorse.mclib.commands.utils.L10n;
import mchorse.mclib.config.ConfigBuilder;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueFloat;
import mchorse.mclib.config.values.ValueInt;
import mchorse.mclib.config.values.ValueRL;
import mchorse.mclib.config.values.ValueString;
import mchorse.mclib.events.RegisterConfigEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

/**
 * Main entry point of Aperture
 *
 * This mod allows people to create Minecraft cinematics. It provides tools for
 * managing camera profiles and camera fixtures withing camera profiles via
 * command line (chat commands) or GUI (camera editor).
 *
 * This mod provides a lot of tools related to camera.
 */
@Mod(modid = Aperture.MOD_ID, name = Aperture.MODNAME, version = Aperture.VERSION, dependencies = "required-after:mclib@[%MCLIB%,)", updateJSON = "https://raw.githubusercontent.com/mchorse/aperture/1.12/version.json")
public class Aperture
{
    /* Mod info */
    public static final String MOD_ID = "aperture";
    public static final String MODNAME = "Aperture";
    public static final String VERSION = "%VERSION%";

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

    public static L10n l10n = new L10n(MOD_ID);

    /* Configuration */
    public static ValueBoolean opCameraEditor;

    public static ValueInt duration;
    public static ValueBoolean spectator;
    public static ValueFloat stepFactor;
    public static ValueFloat rotateFactor;
    public static ValueString commandName;
    public static ValueBoolean debugTicks;
    public static ValueBoolean profileRender;
    public static ValueBoolean profileAutoSave;
    public static ValueBoolean essentialsTeleport;

    public static ValueBoolean outside;
    public static ValueBoolean outsideHidePlayer;
    public static ValueBoolean outsideSky;

    public static ValueBoolean editorSync;
    public static ValueBoolean editorLoop;
    public static ValueBoolean editorOverlay;
    public static ValueRL editorOverlayRL;
    public static ValueBoolean editorF1Tooltip;
    public static ValueBoolean editorDisplayPosition;

    public static ValueInt editorGuidesColor;
    public static ValueBoolean editorRuleOfThirds;
    public static ValueBoolean editorCenterLines;

    public static ValueBoolean editorCrosshair;
    public static ValueBoolean editorLetterbox;
    public static ValueString editorLetterboxAspect;
    public static ValueBoolean editorHideChat;
    public static ValueBoolean editorSeconds;
    public static ValueInt editorAutoSave;

    public static ValueInt flightForward;
    public static ValueInt flightBackward;
    public static ValueInt flightLeft;
    public static ValueInt flightRight;
    public static ValueInt flightUp;
    public static ValueInt flightDown;
    public static ValueInt flightCameraUp;
    public static ValueInt flightCameraDown;
    public static ValueInt flightCameraLeft;
    public static ValueInt flightCameraRight;
    public static ValueInt flightCameraFovMinus;
    public static ValueInt flightCameraFovPlus;
    public static ValueInt flightCameraRollMinus;
    public static ValueInt flightCameraRollPlus;
    public static ValueInt flightCameraSpeedMinus;
    public static ValueInt flightCameraSpeedPlus;

    public static ValueBoolean smoothClampPitch;
    public static ValueBoolean smooth;
    public static ValueFloat smoothFricX;
    public static ValueFloat smoothFricY;
    public static ValueFloat rollFriction;
    public static ValueFloat rollFactor;
    public static ValueFloat fovFriction;
    public static ValueFloat fovFactor;

    public static ValueBoolean minemaDefaultProfileName;
    
    public static ValueBoolean optifineShaderOptionCurve;

    @SubscribeEvent
    public void onConfigRegister(RegisterConfigEvent event)
    {
        opCameraEditor = event.opAccess.category(MOD_ID).getBoolean("camera_editor", true);
        opCameraEditor.syncable();

        /* Aperture's configuration */
        ConfigBuilder builder = event.createBuilder(MOD_ID);

        /* Camera */
        duration = builder.category("general").getInt("duration", 30, 1, 1000);
        spectator = builder.getBoolean("spectator", true);
        stepFactor = builder.getFloat("step_factor", 0.01F, 0, 10);
        rotateFactor = builder.getFloat("rotate_factor", 0.1F, 0, 10);
        commandName = builder.getString("command_name", "camera");
        debugTicks = builder.getBoolean("debug_ticks", false);
        profileRender = builder.getBoolean("profile_render", true);
        profileAutoSave = builder.getBoolean("auto_save", true);
        essentialsTeleport = builder.getBoolean("essentials_tp", false);

        /* Processing camera command name */
        String camera_command_name = commandName.get().trim().replaceAll("[^\\w\\d_\\-]+", "");

        if (camera_command_name.isEmpty())
        {
            commandName.set("camera");
        }

        builder.getCategory().markClientSide();

        /* Camera outside mode */
        outside = builder.category("outside").getBoolean("enabled", false);
        outsideHidePlayer = builder.getBoolean("hide_player", false);
        outsideSky = builder.getBoolean("sky", true);

        builder.getCategory().markClientSide();

        /* Camera editor */
        editorSync = builder.category("editor").getBoolean("sync", false);
        editorLoop = builder.getBoolean("loop", false);
        editorOverlay = builder.getBoolean("overlay", false);
        editorOverlayRL = builder.getRL("overlay_rl", null);
        editorF1Tooltip = builder.getBoolean("f1_tooltip", true);
        editorDisplayPosition = builder.getBoolean("position", false);
        editorGuidesColor = builder.getInt("guides_color", 0xcccc0000).colorAlpha();
        editorRuleOfThirds = builder.getBoolean("rule_of_thirds", false);
        editorCenterLines = builder.getBoolean("center_lines", false);
        editorCrosshair = builder.getBoolean("crosshair", false);
        editorLetterbox = builder.getBoolean("letter_box", false);
        editorLetterboxAspect = builder.getString("aspect_ratio", "21:9");
        editorHideChat = builder.getBoolean("hide_chat", true);
        editorSeconds = builder.getBoolean("seconds", false);
        editorAutoSave = builder.getInt("auto_save", 0, 0, 600);

        builder.getCategory().markClientSide();

        /* Flight mode keybinds */
        flightForward = builder.category("flight").getInt("forward", Keyboard.KEY_W).keybind();
        flightBackward = builder.getInt("backward", Keyboard.KEY_S).keybind();
        flightLeft = builder.getInt("left", Keyboard.KEY_A).keybind();
        flightRight = builder.getInt("right", Keyboard.KEY_D).keybind();
        flightUp = builder.getInt("up", Keyboard.KEY_SPACE).keybind();
        flightDown = builder.getInt("down", Keyboard.KEY_LSHIFT).keybind();
        flightCameraUp = builder.getInt("camera_up", Keyboard.KEY_UP).keybind();
        flightCameraDown = builder.getInt("camera_down", Keyboard.KEY_DOWN).keybind();
        flightCameraLeft = builder.getInt("camera_left", Keyboard.KEY_LEFT).keybind();
        flightCameraRight = builder.getInt("camera_right", Keyboard.KEY_RIGHT).keybind();
        flightCameraFovMinus = builder.getInt("fov_minus", Keyboard.KEY_LBRACKET).keybind();
        flightCameraFovPlus = builder.getInt("fov_plus", Keyboard.KEY_RBRACKET).keybind();
        flightCameraRollMinus = builder.getInt("roll_minus", Keyboard.KEY_APOSTROPHE).keybind();
        flightCameraRollPlus = builder.getInt("roll_plus", Keyboard.KEY_BACKSLASH).keybind();
        flightCameraSpeedMinus = builder.getInt("speed_minus", Keyboard.KEY_O).keybind();
        flightCameraSpeedPlus = builder.getInt("speed_plus", Keyboard.KEY_P).keybind();

        builder.getCategory().markClientSide();

        /* Smooth camera */
        smooth = builder.category("smooth").getBoolean("enabled", false);
        smoothClampPitch = builder.getBoolean("clamp", true);
        smoothFricX = builder.getFloat("x_friction", 0.92F, 0.0F, 1.0F);
        smoothFricY = builder.getFloat("y_friction", 0.92F, 0.0F, 1.0F);
        rollFriction = builder.getFloat("roll_friction", 0.985F, 0.0F, 0.99999F);
        rollFactor = builder.getFloat("roll_speed", 0.01F, 0.0F, 10.0F);
        fovFriction = builder.getFloat("fov_friction", 0.985F, 0.0F, 0.99999F);
        fovFactor = builder.getFloat("fov_speed", 0.075F, 0.0F, 10.0F);

        builder.getCategory().markClientSide();

        this.proxy.registerClientConfig(builder);
    }

    @EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
        McLib.EVENT_BUS.register(this);

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
