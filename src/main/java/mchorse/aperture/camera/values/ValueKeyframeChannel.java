package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ValueKeyframeChannel extends Value
{
    private KeyframeChannel channel = new KeyframeChannel();

    public ValueKeyframeChannel(String id)
    {
        super(id);
    }

    public KeyframeChannel get()
    {
        return this.channel;
    }

    public void set(KeyframeChannel channel)
    {
        this.channel.copy(channel);
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        List<IConfigValue> list = new ArrayList<IConfigValue>();
        int i = 0;

        for (Keyframe keyframe : this.channel.getKeyframes())
        {
            list.add(new ValueKeyframe(this.getId() + "." + i, keyframe));

            i += 1;
        }

        return list;
    }

    @Override
    public Object getValue()
    {
        KeyframeChannel channel = new KeyframeChannel();

        channel.copy(this.channel);

        return channel;
    }

    @Override
    public void setValue(Object value)
    {
        if (value instanceof KeyframeChannel)
        {
            this.set((KeyframeChannel) value);
        }
    }

    @Override
    public void reset()
    {
        this.channel.copy(new KeyframeChannel());
    }

    @Override
    public void resetServer()
    {}

    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiElement> getFields(Minecraft minecraft, GuiConfigPanel guiConfigPanel)
    {
        return null;
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        if (element.isJsonArray())
        {
            this.channel.fromJSON(element.getAsJsonArray());
            this.channel.sort();
        }
    }

    @Override
    public JsonElement toJSON()
    {
        return this.channel.toJSON();
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValueKeyframeChannel)
        {
            this.set(((ValueKeyframeChannel) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.channel.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.channel.toBytes(buffer);
    }
}