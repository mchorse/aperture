package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraAPI;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraProfileList;
import mchorse.aperture.network.common.PacketRequestCameraProfiles;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerRequestCameraProfiles extends ServerMessageHandler<PacketRequestCameraProfiles>
{
    @Override
    public void run(EntityPlayerMP player, PacketRequestCameraProfiles message)
    {
        Dispatcher.sendTo(new PacketCameraProfileList(CameraAPI.getServerProfiles()), player);
    }
}