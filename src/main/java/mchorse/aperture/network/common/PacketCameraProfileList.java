package mchorse.aperture.network.common;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketCameraProfileList implements IMessage
{
    public List<String> cameras;

    public PacketCameraProfileList()
    {
        this.cameras = new ArrayList<String>();
    }

    public PacketCameraProfileList(List<String> cameras)
    {
        this.cameras = cameras;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        for (int i = 0, c = buf.readInt(); i < c; i++)
        {
            this.cameras.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.cameras.size());

        for (String str : this.cameras)
        {
            ByteBufUtils.writeUTF8String(buf, str);
        }
    }
}