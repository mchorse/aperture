package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketRemoveCameraProfile;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerRemoveCameraProfile extends ServerMessageHandler<PacketRemoveCameraProfile>
{
    @Override
    public void run(EntityPlayerMP player, PacketRemoveCameraProfile message)
    {
        if (CameraUtils.removeProfile(message.profile))
        {
            Dispatcher.sendTo(message, player);
        }
    }
}