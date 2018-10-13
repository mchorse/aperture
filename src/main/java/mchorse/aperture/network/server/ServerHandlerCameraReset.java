package mchorse.aperture.network.server;

import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.network.common.PacketCameraReset;
import mchorse.mclib.network.ServerMessageHandler;
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
        ICamera camera = Camera.get(player);

        camera.setCurrentProfile("");
        camera.setCurrentProfileTimestamp(-1);
    }
}