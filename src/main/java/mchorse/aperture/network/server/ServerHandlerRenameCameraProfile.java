package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketRenameCameraProfile;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerRenameCameraProfile extends ServerMessageHandler<PacketRenameCameraProfile>
{
    @Override
    public void run(EntityPlayerMP player, PacketRenameCameraProfile message)
    {
        if (CameraUtils.renameProfile(message.from, message.to))
        {
            Dispatcher.sendTo(message, player);
        }
    }
}