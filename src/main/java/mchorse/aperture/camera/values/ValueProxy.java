package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;

public class ValueProxy extends Value
{
    private IConfigValue proxy;

    public ValueProxy(String id, IConfigValue proxy)
    {
        super(id);

        this.proxy = proxy;
    }

    public IConfigValue getProxy()
    {
        return this.proxy;
    }

    @Override
    public Object getValue()
    {
        return this.proxy.getValue();
    }

    @Override
    public void setValue(Object object)
    {
        this.proxy.setValue(object);
    }

    @Override
    public void reset()
    {
        this.proxy.reset();
    }

    @Override
    public void copy(IConfigValue value)
    {
        this.proxy.copy(value);
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        this.proxy.fromJSON(element);
    }

    @Override
    public JsonElement toJSON()
    {
        return this.proxy.toJSON();
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.proxy.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        this.proxy.toBytes(buffer);
    }
}
