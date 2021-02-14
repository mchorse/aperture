package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketRemoveCameraProfile;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerRemoveCameraProfile extends ServerMessageHandler<PacketRemoveCameraProfile>
{
    @Override
    public void run(EntityPlayerMP player, PacketRemoveCameraProfile message)
    {
        if (!OpHelper.isPlayerOp(player))
        {
            return;
        }

        if (CameraUtils.removeProfile(message.profile))
        {
            Dispatcher.sendTo(message, player);
        }
    }
}