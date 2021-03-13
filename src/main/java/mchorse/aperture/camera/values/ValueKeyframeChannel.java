package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.keyframes.KeyframeChannel;

public class ValueKeyframeChannel extends Value
{
    private KeyframeChannel channel;

    public ValueKeyframeChannel(String id)
    {
        this(id, new KeyframeChannel());
    }

    public ValueKeyframeChannel(String id, KeyframeChannel channel)
    {
        super(id);

        this.channel = channel;
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
    public void fromJSON(JsonElement element)
    {
        if (element.isJsonArray())
        {
            this.channel.fromJSON(element.getAsJsonArray());
            this.channel.sort();
        }
        else if (element.isJsonObject() && element.getAsJsonObject().has("keyframes") && element.getAsJsonObject().get("keyframes").isJsonArray())
        {
            this.channel.fromJSON(element.getAsJsonObject().get("keyframes").getAsJsonArray());
            this.channel.sort();
        }
    }

    @Override
    public JsonElement toJSON()
    {
        return this.channel.toJSON();
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueKeyframeChannel)
        {
            this.set(((ValueKeyframeChannel) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.channel.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        this.channel.toBytes(buffer);
    }
}