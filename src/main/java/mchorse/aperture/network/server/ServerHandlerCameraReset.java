package mchorse.aperture.network.server;

import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.network.common.PacketCameraReset;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Server handler camera reset
 *
 * Reset camera profile
 */
public class ServerHandlerCameraReset extends ServerMessageHandler<PacketCameraReset>
{
    @Override
    public void run(EntityPlayerMP player, PacketCameraReset message)
    {
        ICamera recording = Camera.get(player);

        recording.setCurrentProfile("");
        recording.setCurrentProfileTimestamp(-1);
    }
}