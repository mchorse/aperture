package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.ICamera;
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
            ICamera cap = Camera.get(player);

            cap.setCurrentProfile(message.from);
            cap.setCurrentProfileTimestamp(System.currentTimeMillis());
        }
    }
}