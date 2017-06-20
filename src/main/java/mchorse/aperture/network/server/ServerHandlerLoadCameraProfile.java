package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.network.common.PacketLoadCameraProfile;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerLoadCameraProfile extends ServerMessageHandler<PacketLoadCameraProfile>
{
    @Override
    public void run(EntityPlayerMP player, PacketLoadCameraProfile message)
    {
        CameraUtils.sendProfileToPlayer(message.filename, player, false, message.force);
    }
}
