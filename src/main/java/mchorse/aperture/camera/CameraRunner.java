package mchorse.aperture.camera;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.GameType;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Profile runner
 *
 * This class is responsible for running camera profiles (i.e. applying current's
 * fixture camera transformations on player).
 */
@SideOnly(Side.CLIENT)
public class CameraRunner
{
    private Minecraft mc = Minecraft.getMinecraft();

    /**
     * FOV used before camera playback
     */
    private float lastFov = 70.0F;

    /**
     * Roll used before camera playback
     */
    private float lastRoll = 0;

    /**
     * Game mode player was in before playback
     */
    private GameType gameMode = GameType.NOT_SET;

    /**
     * Whether it's first tick during the playback
     */
    private boolean firstTick = false;

    /**
     * Is camera runner waits for 0.0 partial tick
     */
    private boolean firstTickZero = false;

    /**
     * Whether partial tick 0.0 was detected
     */
    private boolean firstTickZeroStart = false;

    /**
     * Is camera runner running?
     */
    private boolean isRunning = false;

    /**
     * The duration of camera profile
     */
    private long duration;

    /**
     * Camera profile which is getting currently played
     */
    private CameraProfile profile;

    /**
     * Position used to apply fixtures and modifiers upon
     */
    private Position position = new Position(0, 0, 0, 0, 0);

    /**
     * How many ticks passed since the beginning
     */
    public long ticks;

    /**
     * This field is responsible for handling the outside mode 
     */
    public CameraOutside outside = new CameraOutside();

    /* Used by camera renderer */
    public float yaw = 0.0F;
    public float pitch = 0.0F;

    /* Profile access methods */

    public boolean isRunning()
    {
        return this.isRunning;
    }

    public Position getPosition()
    {
        return this.position;
    }

    /**
     * Get game mode of the given player
     */
    public GameType getGameMode(EntityPlayer player)
    {
        NetworkPlayerInfo networkplayerinfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(player.getGameProfile().getId());

        return networkplayerinfo != null ? networkplayerinfo.getGameType() : GameType.CREATIVE;
    }

    /* Playback methods (start/stop) */

    public void toggle(CameraProfile profile, long ticks)
    {
        if (this.isRunning)
        {
            this.stop();
        }
        else
        {
            this.start(profile, ticks);
        }
    }

    /**
     * Start the profile runner from the first tick
     */
    public void start(CameraProfile profile)
    {
        this.start(profile, 0);
    }

    /**
     * Start the profile runner. This method also responsible for setting
     * important values before starting the run (like setting duration, and
     * reseting ticks).
     */
    public void start(CameraProfile profile, long start)
    {
        if (profile == null || profile.getCount() == 0)
        {
            return;
        }

        this.profile = profile;

        if (!this.isRunning)
        {
            this.lastFov = this.mc.gameSettings.fovSetting;
            this.lastRoll = ClientProxy.control.roll;
            this.gameMode = this.getGameMode(this.mc.player);
            this.position.set(this.mc.player);

            if (Aperture.proxy.config.camera_spectator && !Aperture.proxy.config.camera_outside && this.gameMode != GameType.SPECTATOR)
            {
                this.mc.player.sendChatMessage("/gamemode 3");
            }

            MinecraftForge.EVENT_BUS.register(this);

            this.attachOutside();
        }

        this.position.set(this.mc.player);

        this.isRunning = true;
        this.duration = this.profile.getDuration();
        this.ticks = start;

        this.firstTick = true;
        this.firstTickZero = Aperture.proxy.config.camera_first_tick_zero;
        this.firstTickZeroStart = false;
    }

    /**
     * Stop playback of camera profile
     */
    public void stop()
    {
        if (this.isRunning)
        {
            if (this.mc.player != null)
            {
                if (Aperture.proxy.config.camera_spectator && !Aperture.proxy.config.camera_outside && this.gameMode != GameType.SPECTATOR)
                {
                    this.mc.player.sendChatMessage("/gamemode " + this.gameMode.getID());
                }

                if (Aperture.proxy.config.camera_minema)
                {
                    ClientCommandHandler.instance.executeCommand(this.mc.player, "/minema disable");
                }
            }

            this.mc.gameSettings.fovSetting = this.lastFov;
            ClientProxy.control.roll = this.lastRoll;
            this.gameMode = null;

            MinecraftForge.EVENT_BUS.unregister(this);

            this.detachOutside();
        }

        this.isRunning = false;
        this.profile = null;

        ClientProxy.control.resetRoll();
    }

    /**
     * Attach outside mode handler 
     */
    public void attachOutside()
    {
        if (!this.outside.active && Aperture.proxy.config.camera_outside)
        {
            this.outside.start();
        }
    }

    /**
     * Detach outside mode handler 
     */
    public void detachOutside()
    {
        if (this.outside.active)
        {
            this.outside.stop();
        }
    }

    /**
     * The method that does the most exciting thing! This method is responsible
     * for applying interpolated fixture on position and apply the output from
     * fixture onto player.
     */
    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event)
    {
        if (this.profile == null || this.mc.player == null)
        {
            this.stop();

            return;
        }

        if (event.phase == Phase.END)
        {
            if (this.outside.active)
            {
                this.mc.setRenderViewEntity(Aperture.proxy.config.camera_outside_sky ? this.mc.player : this.outside.camera);
            }

            return;
        }

        if (this.firstTick)
        {
            /* Currently Minema supports client side /minema command which
             * record video */
            if (Aperture.proxy.config.camera_minema)
            {
                ClientCommandHandler.instance.executeCommand(this.mc.player, "/minema enable");
            }

            this.firstTick = false;
        }

        long progress = Math.min(this.ticks, this.duration);

        if (progress >= this.duration)
        {
            this.stop();
        }
        else
        {
            if (this.outside.active)
            {
                this.mc.setRenderViewEntity(Aperture.proxy.config.camera_outside_sky ? this.outside.camera : this.mc.player);
            }

            if (this.firstTickZero && event.renderTickTime == 0.0)
            {
                this.firstTickZeroStart = true;
            }

            if (Aperture.proxy.config.camera_debug_ticks)
            {
                Aperture.LOGGER.info("Camera render frame: " + event.renderTickTime + " " + this.ticks);
            }

            float prevX = this.position.point.x;
            float prevY = this.position.point.y;
            float prevZ = this.position.point.z;

            this.profile.applyProfile(progress, event.renderTickTime, this.position);

            EntityPlayer player = this.mc.player;
            Point point = this.position.point;
            Angle angle = this.position.angle;

            /* Setting up the camera */
            this.mc.gameSettings.fovSetting = angle.fov;
            ClientProxy.control.roll = angle.roll;

            /* Fighting with Optifine disappearing entities bug */
            double y = point.y + Math.sin(progress) * 0.000000001 + 0.000000001;

            /* Velocity simulation (useful for recording the player) */
            if (Aperture.proxy.config.camera_simulate_velocity)
            {
                if (this.ticks == 0)
                {
                    this.setCameraPosition(player, point.x, y, point.z, angle);
                }
                else
                {
                    this.setCameraPosition(player, player.posX, player.posY, player.posZ, angle);
                }

                player.motionX = this.position.point.x - prevX;
                player.motionY = this.position.point.y - prevY;
                player.motionZ = this.position.point.z - prevZ;
            }
            else
            {
                this.setCameraPosition(player, point.x, y, point.z, angle);

                if (!this.outside.active)
                {
                    player.motionX = player.motionY = player.motionZ = 0;
                }
            }

            if (!this.mc.isSingleplayer() && !this.outside.active)
            {
                float dx = point.x - prevX;
                float dy = point.y - prevY;
                float dz = point.z - prevZ;

                if (dx * dx + dy * dy + dz * dz >= 10 * 10)
                {
                    /* Make it compatible with Essentials plugin, which replaced the native /tp command */
                    if (Aperture.proxy.config.minecrafttp_teleport)
                    {
                        this.mc.player.sendChatMessage("/minecraft:tp " + point.x + " " + point.y + " " + point.z + " " + angle.yaw + " " + angle.pitch);
                    }

                    if (Aperture.proxy.config.tp_teleport)
                    {
                        this.mc.player.sendChatMessage("/tp " + point.x + " " + point.y + " " + point.z + " " + angle.yaw + " " + angle.pitch);
                    }
                }
            }

            this.yaw = angle.yaw;
            this.pitch = angle.pitch;

            if (player.isSneaking())
            {
                player.setSneaking(false);
            }
        }
    }

    /**
     * Set camera position
     *
     * This method is responsible for setting camera's position and rotation.
     * Why are these two methods invoked? Good question.
     *
     * {@link Entity#setLocationAndAngles(double, double, double, float, float)}
     * updates some client side values such as lastTick* and prevPos* values,
     * which makes the transition between two distant cameras, seamless, meanwhile
     * {@link Entity#setPositionAndRotation(double, double, double, float, float)}
     * is responsible for fixing/clamping player's rotation.
     *
     * By using only one of these methods wouldn't guarantee supreme 
     * quality of the camera animation.
     */
    public void setCameraPosition(EntityPlayer player, double x, double y, double z, Angle angle)
    {
        EntityLivingBase camera = this.outside.active ? this.outside.camera : player;

        camera.setLocationAndAngles(x, y, z, angle.yaw, angle.pitch);
        camera.setPositionAndRotation(x, y, z, angle.yaw, angle.pitch);
        camera.rotationYawHead = camera.prevRotationYawHead = angle.yaw;
    }

    /**
     * This is going to count ticks (used for camera synchronization)
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.side == Side.CLIENT && event.player == this.mc.player && event.phase == Phase.START)
        {
            if (Aperture.proxy.config.camera_debug_ticks)
            {
                Aperture.LOGGER.info("Camera frame: " + this.ticks);
            }

            if (this.firstTickZero && this.firstTickZeroStart || !this.firstTickZero)
            {
                this.ticks++;
            }
        }
    }
}