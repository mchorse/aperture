package mchorse.aperture;

import mchorse.mclib.McLib;
import mchorse.mclib.config.ConfigBuilder;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueFloat;
import mchorse.mclib.config.values.ValueInt;
import mchorse.mclib.config.values.ValueRL;
import mchorse.mclib.config.values.ValueString;
import mchorse.mclib.events.RegisterConfigEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import mchorse.aperture.commands.CommandAperture;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;

/**
 * Main entry point of Aperture
 *
 * This mod allows people to create Minecraft cinematics. It provides tools for
 * managing camera profiles and camera fixtures withing camera profiles via
 * command line (chat commands) or GUI (camera editor).
 *
 * This mod provides a lot of tools related to camera.
 */
@Mod(modid = Aperture.MOD_ID, name = Aperture.MODNAME, version = Aperture.VERSION, dependencies = "required-after:mclib@[%MCLIB%,)", updateJSON = "https://raw.githubusercontent.com/mchorse/aperture/master/version.json")
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

    /* Configuration */
    public static ValueInt durationStep;
    public static ValueInt duration;
    public static ValueBoolean spectator;
    public static ValueFloat stepFactor;
    public static ValueFloat rotateFactor;
    public static ValueString commandName;
    public static ValueBoolean simulateVelocity;
    public static ValueBoolean debugTicks;
    public static ValueBoolean firstTickZero;
    public static ValueBoolean profileRender;
    public static ValueBoolean profileAutoSave;
    public static ValueBoolean essentialsTeleport;

    public static ValueBoolean outside;
    public static ValueBoolean outsideHidePlayer;
    public static ValueBoolean outsideSky;

    public static ValueBoolean editorOverlay;
    public static ValueRL editorOverlayRL;
    public static ValueBoolean editorF1Tooltip;
    public static ValueString editorLetterboxAspect;

    public static ValueBoolean smoothClampPitch;
    public static ValueBoolean smooth;
    public static ValueFloat smoothFricX;
    public static ValueFloat smoothFricY;
    public static ValueFloat rollFriction;
    public static ValueFloat rollFactor;
    public static ValueFloat fovFriction;
    public static ValueFloat fovFactor;

    @SubscribeEvent
    public void onConfigRegister(RegisterConfigEvent event)
    {
        ConfigBuilder builder = event.createBuilder(MOD_ID);

        /* Camera */
        durationStep = builder.category("general").getInt("camera_duration_step", 10, 1, 100);
        duration = builder.getInt("camera_duration", 30, 1, 1000);
        spectator = builder.getBoolean("camera_spectator", true);
        stepFactor = builder.getFloat("camera_step_factor", 0.01F, 0, 10);
        rotateFactor = builder.getFloat("camera_rotate_factor", 0.1F, 0, 10);
        commandName = builder.getString("camera_command_name", "camera");
        simulateVelocity = builder.getBoolean("camera_simulate_velocity", false);
        debugTicks = builder.getBoolean("camera_debug_ticks", false);
        firstTickZero = builder.getBoolean("camera_first_tick_zero", false);
        profileRender = builder.getBoolean("camera_profile_render", true);
        profileAutoSave = builder.getBoolean("camera_auto_save", true);
        essentialsTeleport = builder.getBoolean("minecrafttp_teleport", false);

        /* Processing camera command name */
        String camera_command_name = commandName.get().trim().replaceAll("[^\\w\\d_\\-]+", "");

        if (camera_command_name.isEmpty())
        {
            commandName.set("camera");
        }

        /* Camera outside mode */
        outside = builder.category("outside").getBoolean("camera_outside", false);
        outsideHidePlayer = builder.getBoolean("camera_outside_hide_player", false);
        outsideSky = builder.getBoolean("camera_outside_sky", true);

        /* Camera editor overlay */
        editorOverlay = builder.category("overlay").getBoolean("camera_editor_overlay", false);
        editorOverlayRL = builder.getRL("camera_editor_overlay_rl", null);
        editorF1Tooltip = builder.getBoolean("camera_editor_f1_tooltip", true);
        editorLetterboxAspect = builder.getString("aspect_ratio", "16:9");

        /* Smooth camera */
        smooth = builder.category("smooth").getBoolean("smooth_enabled", false);
        smoothClampPitch = builder.getBoolean("camera_smooth_clamp", true);
        smoothFricX = builder.getFloat("mouse_x_friction", 0.92F, 0.0F, 1.0F);
        smoothFricY = builder.getFloat("mouse_y_friction", 0.92F, 0.0F, 1.0F);

        rollFriction = builder.getFloat("roll_friction", 0.985F, 0.0F, 0.99999F);
        rollFactor = builder.getFloat("roll_speed", 0.01F, 0.0F, 10.0F);

        fovFriction = builder.getFloat("fov_friction", 0.985F, 0.0F, 0.99999F);
        fovFactor = builder.getFloat("fov_speed", 0.075F, 0.0F, 10.0F);

        event.modules.add(builder.build());
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
