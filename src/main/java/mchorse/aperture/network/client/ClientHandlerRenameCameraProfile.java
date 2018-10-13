package mchorse.aperture.network.client;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.network.common.PacketRenameCameraProfile;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerRenameCameraProfile extends ClientMessageHandler<PacketRenameCameraProfile>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketRenameCameraProfile message)
    {
        ClientProxy.cameraEditor.profiles.rename(new ServerDestination(message.from), message.to);
    }
}