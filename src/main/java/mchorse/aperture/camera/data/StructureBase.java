package mchorse.aperture.camera.data;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.config.ConfigCategory;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.network.IByteBufSerializable;

/**
 * Abstract structure base that supports configuration via
 * McLib's value API
 */
public abstract class StructureBase implements IByteBufSerializable
{
    protected ConfigCategory category = new ConfigCategory("");

    protected void register(IConfigValue value)
    {
        this.category.values.put(value.getId(), value);
    }

    public void copy(StructureBase base)
    {
        for (IConfigValue value : this.category.values.values())
        {
            IConfigValue from = base.category.values.get(value.getId());

            if (from != null)
            {
                value.copy(from);
            }
        }
    }

    /* JSON (de)serialization methods */

    public void fromJSON(JsonObject object)
    {
        for (IConfigValue value : this.category.values.values())
        {
            if (object.has(value.getId()))
            {
                value.fromJSON(object.get(value.getId()));
            }
        }
    }

    public void toJSON(JsonObject object)
    {
        for (IConfigValue value : this.category.values.values())
        {
            object.add(value.getId(), value.toJSON());
        }
    }

    /* ByteBuf (de)serialization methods */

    /**
     * Read abstract fixture's properties from byte buffer
     */
    @Override
    public void fromBytes(ByteBuf buffer)
    {
        for (IConfigValue value : this.category.values.values())
        {
            value.fromBytes(buffer);
        }
    }

    /**
     * Write this abstract fixture to the byte buffer
     */
    @Override
    public void toBytes(ByteBuf buffer)
    {
        for (IConfigValue value : this.category.values.values())
        {
            value.toBytes(buffer);
        }
    }
}