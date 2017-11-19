package mchorse.aperture.network.client;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.network.common.PacketRenameCameraProfile;
import net.minecraft.client.entity.EntityPlayerSP;

public class ClientHandlerRenameCameraProfile extends ClientMessageHandler<PacketRenameCameraProfile>
{
    @Override
    public void run(EntityPlayerSP player, PacketRenameCameraProfile message)
    {
        ClientProxy.cameraEditor.profiles.rename(new ServerDestination(message.from), message.to);
    }
}