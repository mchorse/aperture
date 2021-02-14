package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraAPI;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraProfileList;
import mchorse.aperture.network.common.PacketRequestCameraProfiles;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerRequestCameraProfiles extends ServerMessageHandler<PacketRequestCameraProfiles>
{
    @Override
    public void run(EntityPlayerMP player, PacketRequestCameraProfiles message)
    {
        if (!OpHelper.isPlayerOp(player))
        {
            return;
        }

        Dispatcher.sendTo(new PacketCameraProfileList(CameraAPI.getServerProfiles()), player);
    }
}