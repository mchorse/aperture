package mchorse.aperture.network.client;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.network.common.PacketAperture;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerAperture extends ClientMessageHandler<PacketAperture>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP entityPlayerSP, PacketAperture packetAperture)
    {
        ClientProxy.server = true;
    }
}