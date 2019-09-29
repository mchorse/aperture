package mchorse.aperture.config;

import mchorse.aperture.Aperture;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Aperture config class
 *
 * This class stores config properties powered by Forge built-in configuration
 * library.
 *
 * I looked up how this configuration works in Minema mod, so don't wonder if
 * it looks pretty similar. I also looked up Chroonster TestMod3.
 */
public class ApertureConfig
{
    /**
     * Camera duration step (used by keyboard duration bindings)
     */
    public int camera_duration_step;

    /**
     * Default camera duration (used by keyboard fixture bindings)
     */
    public int camera_duration;

    /**
     * Interpolate target fixtures position?
     */
    public boolean camera_interpolate_target;

    /**
     * Ratio for target fixtures interpolation
     */
    public float camera_interpolate_target_value;

    /**
     * Switch to spectator mode when starting camera playback
     */
    public boolean camera_spectator;

    /**
     * Factor for step keys
     */
    public float camera_step_factor;

    /**
     * Factor for rotate keys
     */
    public float camera_rotate_factor;

    /**
     * Activate Minema recording on camera start and deactivate on camera stop
     */
    public boolean camera_minema;

    /**
     * Clamp smooth camera's pitch between -90 and 90 degrees range?
     */
    public boolean camera_smooth_clamp;

    /**
     * Default camera path interpolation method
     */
    public String camera_path_default_interp;

    /**
     * Allows you to rebind /camera command's name (if you want to type less)
     */
    public String camera_command_name;

    /**
     * Simulate player's velocity during camera playback
     */
    public boolean camera_simulate_velocity;

    /**
     * Camera debug ticks
     */
    public boolean camera_debug_ticks;

    /**
     * First camera tick must be zero
     */
    public boolean camera_first_tick_zero;

    /**
     * Render camera profile in the world?
     */
    public boolean camera_profile_render;

    /**
     * Aspect ratio for letter box within camera editor
     */
    public String aspect_ratio = "16:9";

    /**
     * Render a custom mouse pointer in GUIs
     */
    public boolean gui_render_mouse;

    /**
     * Use "/minecraft:tp" in multiplayer's playback?
     */
    public boolean minecrafttp_teleport;

    /**
     * Use "/tp" in multiplayer's playback?
     */
    public boolean tp_teleport;

    /**
     * Whether camera should be played back from outside (allowing the 
     * player to act as a body actor)
     */
    public boolean camera_outside;

    /**
     * Whether player should be hidden during playback of the camera 
     * if outside mode is enabled
     */
    public boolean camera_outside_hide_player;

    /**
     * If enabled, it fixes the sky rendering, but hides the block 
     * highlight
     */
    public boolean camera_outside_sky;

    /**
     * Enables camera editor overlay
     */
    public boolean camera_editor_overlay;

    /**
     * Resource location path to the texture which will be used as 
     * overlay 
     */
    public String camera_editor_overlay_rl = "";

    /**
     * Save all camera profiles upon exiting the world
     */
    public boolean camera_auto_save;

    /* Non conifg option stuff */

    /**
     * Forge configuration
     */
    public Configuration config;

    public ApertureConfig(Configuration config)
    {
        this.config = config;
        this.reload();
    }

    /**
     * Reload config values
     */
    public void reload()
    {
        String camera = "camera";
        String outside = "outside";
        String overlay = "overlay";
        String prefix = "aperture.config.camera.";

        /* Camera */
        this.camera_duration_step = this.getInt("camera_duration_step", camera, 10, 1, 100, "What is default step to use when adding or reducing duration of the camera fixture (in ticks)");
        this.camera_duration = this.getInt("camera_duration", camera, 30, 1, 1000, "What is default duration of the camera fixture (in ticks)");
        this.camera_interpolate_target = this.getBoolean("camera_interpolate_target", camera, false, "Interpolate target based camera fixtures (follow and look) outcome");
        this.camera_interpolate_target_value = this.getFloat("camera_interpolate_target_value", camera, 0.5F, 0.0F, 1.0F, "Interpolation value for target based camera fixture interpolation");
        this.camera_spectator = this.getBoolean("camera_spectator", camera, true, "Switch to spectator mode when starting camera playback");
        this.camera_step_factor = this.getFloat("camera_step_factor", camera, 0.01F, 0, 10, "Camera step factor for step keys");
        this.camera_rotate_factor = this.getFloat("camera_rotate_factor", camera, 0.1F, 0, 10, "Camera rotate factor for rotate keys");
        this.camera_minema = this.getBoolean("camera_minema", camera, false, "Activate Minema recording on camera start and deactivate on camera stop");
        this.camera_path_default_interp = this.getString("camera_path_default_interp", camera, "linear", "Default interpolation method for path fixture");
        this.camera_command_name = this.getString("camera_command_name", camera, "camera", "Allows you to rebind camera command's name (requires game reload to take effect)");
        this.camera_simulate_velocity = this.getBoolean("camera_simulate_velocity", camera, false, "Simulate player's velocity during camera playback (see legs in perspective)");
        this.camera_debug_ticks = this.getBoolean("camera_debug_ticks", camera, false, "Write ticks to the log during camera playback");
        this.camera_first_tick_zero = this.getBoolean("camera_first_tick_zero", camera, false, "When camera runner starts, start the actual playback when partial tick is exactly zero");
        this.camera_profile_render = this.getBoolean("camera_profile_render", camera, true, "Render camera profile in the world?");
        this.camera_auto_save = this.getBoolean("camera_auto_save", camera, true, "Save all camera profiles upon exiting the world");
        this.minecrafttp_teleport = this.getBoolean("minecrafttp_teleport", camera, true, "When start the camera playback in multiplayer, teleport you with /minecraft:tp command (For Essentials)");
        this.tp_teleport = this.getBoolean("tp_teleport", camera, true, "When start the camera playback in multiplayer, teleport you with /tp command (For Vanilla or Forge)");
        this.aspect_ratio = this.getString("aspect_ratio", camera, "16:9", "Aspect ratio for camera editor's letter box");

        /* Camera outside mode */
        this.camera_outside = this.getBoolean("camera_outside", outside, false, "Whether camera should be played back from outside (allowing the player to act as a body actor)");
        this.camera_outside_hide_player = this.getBoolean("camera_outside_hide_player", outside, false, "Whether player should be hidden during playback of the camera if outside mode is enabled");
        this.camera_outside_sky = this.getBoolean("camera_outside_sky", outside, true, "If enabled, it fixes the sky rendering, but hides the block highlight");

        /* Camera editor overlay */
        this.camera_editor_overlay = this.getBoolean("camera_editor_overlay", overlay, false, "Enables camera editor overlay");
        this.camera_editor_overlay_rl = this.getString("camera_editor_overlay_rl", overlay, "", "Resource location path to the texture which will be used as overlay");

        /* Processing camera command name */
        this.camera_command_name = this.camera_command_name.trim().replaceAll("[^\\w\\d_\\-]+", "");

        /* Mouse rendering */
        this.gui_render_mouse = this.config.getBoolean("gui_render_mouse", camera, false, "Render a custom mouse pointer in GUIs", prefix + "gui_render_mouse");

        if (this.camera_command_name.isEmpty())
        {
            this.camera_command_name = "camera";
        }

        /* Smooth camera */
        this.camera_smooth_clamp = this.config.getBoolean("camera_smooth_clamp", "smooth", true, "Clip smooth camera's pitch between -90 and 90 degrees range?", "aperture.config.smooth.camera_smooth_clamp");

        Aperture.proxy.onConfigChange(this);

        if (this.config.hasChanged())
        {
            this.config.save();
        }
    }

    public boolean getBoolean(String name, String category, boolean defaultValue, String comment)
    {
        String langKey = "aperture.config." + category + "." + name;
        String commentKey = "aperture.config.comments." + category + "." + name;

        return this.config.getBoolean(name, category, defaultValue, Aperture.proxy.getLanguageString(commentKey, comment), langKey);
    }

    public int getInt(String name, String category, int defaultValue, int minValue, int maxValue, String comment)
    {
        String langKey = "aperture.config." + category + "." + name;
        String commentKey = "aperture.config.comments." + category + "." + name;

        return this.config.getInt(name, category, defaultValue, minValue, maxValue, Aperture.proxy.getLanguageString(commentKey, comment), langKey);
    }

    public float getFloat(String name, String category, float defaultValue, float minValue, float maxValue, String comment)
    {
        String langKey = "aperture.config." + category + "." + name;
        String commentKey = "aperture.config.comments." + category + "." + name;

        return this.config.getFloat(name, category, defaultValue, minValue, maxValue, Aperture.proxy.getLanguageString(commentKey, comment), langKey);
    }

    public String getString(String name, String category, String defaultValue, String comment)
    {
        String langKey = "aperture.config." + category + "." + name;
        String commentKey = "aperture.config.comments." + category + "." + name;

        return this.config.getString(name, category, defaultValue, Aperture.proxy.getLanguageString(commentKey, comment), langKey);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Aperture.MODID) && this.config.hasChanged())
        {
            this.reload();
        }
    }
}