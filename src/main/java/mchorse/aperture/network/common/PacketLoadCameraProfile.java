package mchorse.aperture.network.common;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketLoadCameraProfile implements IMessage
{
    public String filename;
    public boolean force;

    public PacketLoadCameraProfile()
    {}

    public PacketLoadCameraProfile(String filename, boolean force)
    {
        this.filename = filename;
        this.force = force;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.filename = ByteBufUtils.readUTF8String(buf);
        this.force = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.filename);
        buf.writeBoolean(force);
    }
}
