package mchorse.aperture.network.client;

import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.client.gui.dashboard.GuiCameraDashboard;
import mchorse.aperture.network.common.PacketRemoveCameraProfile;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerRemoveCameraProfile extends ClientMessageHandler<PacketRemoveCameraProfile>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketRemoveCameraProfile message)
    {
        GuiCameraDashboard.getCameraEditor().camera.profiles.remove(new ServerDestination(message.profile));
    }
}