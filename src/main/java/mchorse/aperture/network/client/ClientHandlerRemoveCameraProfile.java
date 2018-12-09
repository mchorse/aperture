package mchorse.aperture.network.client;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.network.common.PacketRemoveCameraProfile;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;

public class ClientHandlerRemoveCameraProfile extends ClientMessageHandler<PacketRemoveCameraProfile>
{
    @Override
    public void run(EntityPlayerSP player, PacketRemoveCameraProfile message)
    {
        ClientProxy.getCameraEditor().profiles.remove(new ServerDestination(message.profile));
    }
}