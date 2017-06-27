package mchorse.aperture.network.common;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketCameraState implements IMessage
{
    public String filename = "";
    public boolean toPlay;

    public PacketCameraState()
    {}

    public PacketCameraState(boolean toPlay)
    {
        this.toPlay = toPlay;
    }

    public PacketCameraState(String filename, boolean toPlay)
    {
        this.filename = filename;
        this.toPlay = toPlay;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.filename = ByteBufUtils.readUTF8String(buf);
        this.toPlay = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.filename);
        buf.writeBoolean(this.toPlay);
    }
}
