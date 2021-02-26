package mchorse.aperture.camera.data;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.config.ConfigCategory;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.network.IByteBufSerializable;

import java.util.Collection;

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

    public Collection<IConfigValue> getProperties()
    {
        return this.category.values.values();
    }

    public IConfigValue getProperty(String name)
    {
        IConfigValue value = this.category.values.get(name);

        if (value == null && name.contains("."))
        {
            String[] splits = name.split("\\.");

            value = this.searchRecursively(this.category.values.get(splits[0]), splits, 0, name);
        }

        if (value == null)
        {
            throw new IllegalStateException("Property by name " + name + " can't be found!");
        }

        return value;
    }

    private IConfigValue searchRecursively(IConfigValue value, String[] splits, int i, String name)
    {
        if (value == null)
        {
            return null;
        }

        for (IConfigValue child : value.getSubValues())
        {
            if (child.getId().equals(name))
            {
                return child;
            }
            else if (i + 1 < splits.length && name.startsWith(child.getId()))
            {
                IConfigValue searched = this.searchRecursively(child, splits, i + 1, name);

                if (searched != null)
                {
                    return searched;
                }
            }
        }

        return null;
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