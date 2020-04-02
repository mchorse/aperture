package mchorse.aperture.capabilities;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.CameraProvider;
import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketAperture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

/**
 * Capability handler class
 *
 * This class is responsible for managing capabilities, i.e. attaching
 * capabilities and syncing values on the client.
 */
public class CapabilityHandler
{
    public static final ResourceLocation CAMERA_CAP = new ResourceLocation(Aperture.MOD_ID, "camera_capability");

    /**
     * Attach capabilities (well, only one, right now)
     */
    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof EntityPlayer)) return;

        event.addCapability(CAMERA_CAP, new CameraProvider());
    }

    /**
     * When player logs in, sent him his server counter partner's values.
     */
    @SubscribeEvent
    public void playerLogsIn(PlayerLoggedInEvent event)
    {
        EntityPlayer player = event.player;
        ICamera camera = Camera.get(player);

        if (camera != null && camera.hasProfile())
        {
            CameraUtils.sendProfileToPlayer(camera.currentProfile(), (EntityPlayerMP) player, false, true);

            camera.setCurrentProfileTimestamp(System.currentTimeMillis());
        }

        Dispatcher.sendTo(new PacketAperture(), (EntityPlayerMP) player);
    }
}