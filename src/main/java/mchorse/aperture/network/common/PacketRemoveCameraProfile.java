package mchorse.aperture.network.common;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketRemoveCameraProfile implements IMessage
{
    public String profile;

    public PacketRemoveCameraProfile()
    {}

    public PacketRemoveCameraProfile(String from)
    {
        this.profile = from;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.profile = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.profile);
    }
}