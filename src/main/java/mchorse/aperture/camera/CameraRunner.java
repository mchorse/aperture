package mchorse.aperture.camera;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

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
     * Is camera runner running?
     */
    private boolean isRunning = false;

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
    public Supplier<Long> duration;

    /* Skip first tick update after scrub */
    public boolean skipUpdate = false;

    /* Profile access methods */

    public boolean isRunning()
    {
        return this.isRunning;
    }

    public Position getPosition()
    {
        return this.position;
    }

    /* Playback methods (start/stop) */

    public void toggle(CameraProfile profile, long ticks)
    {
        this.toggle(profile, ticks, null);
    }

    public void toggle(CameraProfile profile, long ticks, Supplier<Long> duration)
    {
        if (this.isRunning)
        {
            this.stop();
        }
        else
        {
            this.start(profile, ticks, duration);
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
     * Start the profile runner from the first tick
     */
    public void start(CameraProfile profile, long ticks)
    {
        this.start(profile, ticks, null);
    }

    /**
     * Start the profile runner. This method also responsible for setting
     * important values before starting the run (like setting duration, and
     * reseting ticks).
     */
    public void start(CameraProfile profile, long start, Supplier<Long> duration)
    {
        if (profile == null)
        {
            return;
        }

        this.profile = profile;

        if (!this.isRunning)
        {
            ClientProxy.control.cache();
            this.position.set(this.mc.player);

            if (Aperture.spectator.get() && !Aperture.outside.get() && EntityUtils.getGameMode() != GameType.SPECTATOR)
            {
                this.mc.player.sendChatMessage("/gamemode 3");
            }

            MinecraftForge.EVENT_BUS.register(this);

            this.attachOutside();
        }

        this.position.set(this.mc.player);

        this.isRunning = true;
        this.ticks = start;
        this.duration = duration;
        this.skipUpdate = false;
    }

    /**
     * Stop playback of camera profile
     */
    public void stop()
    {
        if (this.isRunning)
        {
            ClientProxy.control.restore();
            MinecraftForge.EVENT_BUS.unregister(this);

            this.detachOutside();
        }

        this.isRunning = false;
        this.profile = null;
        this.duration = null;
        this.skipUpdate = false;

        ClientProxy.control.resetRoll();
    }

    /**
     * Attach outside mode handler 
     */
    public void attachOutside()
    {
        if (!this.outside.active && Aperture.outside.get())
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
                this.mc.setRenderViewEntity(Aperture.outsideSky.get() ? this.mc.player : this.outside.camera);
            }

            return;
        }

        long profileDuration = this.profile.getDuration();
        long duration = this.duration == null ? profileDuration : this.duration.get();
        long progress = this.ticks;

        if (progress >= duration)
        {
            this.stop();
        }
        else
        {
            if (this.outside.active)
            {
                this.mc.setRenderViewEntity(Aperture.outsideSky.get() ? this.outside.camera : this.mc.player);
            }

            if (Aperture.debugTicks.get())
            {
                Aperture.LOGGER.info("Camera render frame: " + event.renderTickTime + " " + this.ticks);
            }

            double prevX = this.position.point.x;
            double prevY = this.position.point.y;
            double prevZ = this.position.point.z;

            this.profile.applyCurves(progress, event.renderTickTime);

            if (progress < profileDuration)
            {
                this.profile.applyProfile(progress, event.renderTickTime, this.position);
            }

            EntityPlayer player = this.mc.player;
            Point point = this.position.point;
            Angle angle = this.position.angle;

            /* Setting up the camera */
            this.mc.gameSettings.fovSetting = angle.fov;
            ClientProxy.control.roll = angle.roll;

            /* Fighting with Optifine disappearing entities bug */
            double y = point.y + Math.sin(progress) * 0.000000001 + 0.000000001;

            /* Velocity simulation (useful for recording the player) */
            this.setCameraPosition(player, point.x, y, point.z, angle);

            if (!this.outside.active)
            {
                player.motionX = player.motionY = player.motionZ = 0;
            }

            if (!this.mc.isSingleplayer() && !this.outside.active)
            {
                double dx = point.x - prevX;
                double dy = point.y - prevY;
                double dz = point.z - prevZ;

                if (dx * dx + dy * dy + dz * dz >= 10 * 10)
                {
                    /* Make it compatible with Essentials plugin, which replaced the native /tp command */
                    if (Aperture.essentialsTeleport.get())
                    {
                        this.mc.player.sendChatMessage("/minecraft:tp " + point.x + " " + point.y + " " + point.z + " " + angle.yaw + " " + angle.pitch);
                    }
                    else
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

        camera.setLocationAndAngles(x, y - camera.getEyeHeight(), z, angle.yaw % 360, angle.pitch);
        camera.setPositionAndRotation(x, y - camera.getEyeHeight(), z, angle.yaw % 360, angle.pitch);
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
            if (Aperture.debugTicks.get())
            {
                Aperture.LOGGER.info("Camera frame: " + this.ticks);
            }

            if (this.skipUpdate)
            {
                this.skipUpdate = false;
            }
            else
            {
                this.ticks++;
            }
        }
    }
}