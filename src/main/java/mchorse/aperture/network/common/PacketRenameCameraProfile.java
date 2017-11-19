package mchorse.aperture.network.common;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketRenameCameraProfile implements IMessage
{
    public String from;
    public String to;

    public PacketRenameCameraProfile()
    {}

    public PacketRenameCameraProfile(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.from = ByteBufUtils.readUTF8String(buf);
        this.to = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.from);
        ByteBufUtils.writeUTF8String(buf, this.to);
    }
}