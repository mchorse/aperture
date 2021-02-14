package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.network.common.PacketLoadCameraProfile;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerLoadCameraProfile extends ServerMessageHandler<PacketLoadCameraProfile>
{
    @Override
    public void run(EntityPlayerMP player, PacketLoadCameraProfile message)
    {
        if (!OpHelper.isPlayerOp(player))
        {
            return;
        }

        CameraUtils.sendProfileToPlayer(message.filename, player, false, message.force);
    }
}