package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValueModifier extends Value
{
    private AbstractModifier modifier;

    public ValueModifier(String id, AbstractModifier modifier)
    {
        super(id);

        this.modifier = modifier;
    }

    public AbstractModifier get()
    {
        return this.modifier;
    }

    public void set(AbstractModifier modifier)
    {
        if (modifier != null)
        {
            this.modifier = modifier.copy();
        }
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        List<IConfigValue> values = new ArrayList<IConfigValue>();

        for (IConfigValue value : this.modifier.getProperties())
        {
            values.add(new ValueProxy(this.getId() + "." + value.getId(), value));
        }

        return values;
    }

    @Override
    public Object getValue()
    {
        return this.modifier.copy();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof AbstractModifier)
        {
            this.set((AbstractModifier) object);
        }
    }

    @Override
    public void reset()
    {
        this.modifier = null;
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValueModifier)
        {
            this.setValue(((ValueModifier) value).get());
        }
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        if (!element.isJsonObject())
        {
            return;
        }

        JsonObject object = element.getAsJsonObject();
        AbstractModifier modifier = ModifierRegistry.fromJSON(object);

        if (modifier != null)
        {
            this.modifier = modifier;
        }
    }

    @Override
    public JsonElement toJSON()
    {
        return ModifierRegistry.toJSON(this.modifier);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.modifier = ModifierRegistry.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        ModifierRegistry.toBytes(this.modifier, buffer);
    }
}
