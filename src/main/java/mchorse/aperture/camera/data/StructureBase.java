package mchorse.aperture.camera.data;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.network.IByteBufSerializable;

import java.util.Collection;

/**
 * Abstract structure base that supports configuration via
 * McLib's value API
 */
public abstract class StructureBase implements IByteBufSerializable
{
    protected Value category = new Value("");

    protected void register(Value value)
    {
        this.category.addSubValue(value);
    }

    public Collection<Value> getProperties()
    {
        return this.category.getSubValues();
    }

    public Value getProperty(String name)
    {
        Value value = this.category.getSubValue(name);

        if (value == null && name.contains("."))
        {
            String[] splits = name.split("\\.");

            value = this.searchRecursively(splits, name);
        }

        if (value == null)
        {
            throw new IllegalStateException("Property by name " + name + " can't be found!");
        }

        return value;
    }

    private Value searchRecursively(String[] splits, String name)
    {
        int i = 0;
        Value current = this.category.getSubValue(splits[i]);

        while (current != null && i < splits.length - 1)
        {
            i += 1;
            current = current.getSubValue(splits[i]);
        }

        if (current == null)
        {
            return null;
        }

        return current.getPath().equals(name) ? current : null;
    }

    public void copy(StructureBase base)
    {
        for (Value value : this.category.getSubValues())
        {
            Value from = base.category.getSubValue(value.id);

            if (from != null)
            {
                value.copy(from);
            }
        }
    }

    /* JSON (de)serialization methods */

    public void fromJSON(JsonObject object)
    {
        for (Value value : this.category.getSubValues())
        {
            if (object.has(value.id))
            {
                value.fromJSON(object.get(value.id));
            }
        }
    }

    public void toJSON(JsonObject object)
    {
        for (Value value : this.category.getSubValues())
        {
            object.add(value.id, value.toJSON());
        }
    }

    /* ByteBuf (de)serialization methods */

    /**
     * Read abstract fixture's properties from byte buffer
     */
    @Override
    public void fromBytes(ByteBuf buffer)
    {
        for (Value value : this.category.getSubValues())
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
        for (Value value : this.category.getSubValues())
        {
            value.toBytes(buffer);
        }
    }
}