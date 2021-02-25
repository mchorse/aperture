package mchorse.aperture.camera.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValueModifiers extends Value
{
    public List<AbstractModifier> modifiers = new ArrayList<AbstractModifier>();

    public ValueModifiers(String id)
    {
        super(id);
    }

    public List<AbstractModifier> get()
    {
        return this.modifiers;
    }

    public void set(List<AbstractModifier> modifiers)
    {
        this.modifiers.clear();

        for (AbstractModifier modifier : modifiers)
        {
            this.modifiers.add(modifier.copy());
        }
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        List<IConfigValue> values = new ArrayList<IConfigValue>();
        int i = 0;

        for (AbstractModifier modifier : this.modifiers)
        {
            values.add(new ValueModifier(this.getId() + "." + i, modifier));

            i += 1;
        }

        return values;
    }

    @Override
    public Object getValue()
    {
        List<AbstractModifier> modifiers = new ArrayList<AbstractModifier>();

        for (AbstractModifier modifier : this.modifiers)
        {
            modifiers.add(modifier.copy());
        }

        return modifiers;
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof List)
        {
            List list = (List) object;

            if (list.isEmpty() || list.get(0) instanceof AbstractModifier)
            {
                this.set((List<AbstractModifier>) list);
            }
        }
    }

    @Override
    public void reset()
    {
        this.modifiers.clear();
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValueModifiers)
        {
            this.set(((ValueModifiers) value).get());
        }
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        if (!element.isJsonArray())
        {
            return;
        }

        JsonArray array = element.getAsJsonArray();

        this.modifiers.clear();

        for (JsonElement jsonElement : array)
        {
            if (!jsonElement.isJsonObject())
            {
                continue;
            }

            JsonObject object = jsonElement.getAsJsonObject();
            AbstractModifier modifier = ModifierRegistry.fromJSON(object);

            if (modifier != null)
            {
                this.modifiers.add(modifier);
            }
        }
    }

    @Override
    public JsonElement toJSON()
    {
        JsonArray array = new JsonArray();

        for (AbstractModifier modifier : this.modifiers)
        {
            array.add(ModifierRegistry.toJSON(modifier));
        }

        return array;
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.modifiers.clear();

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractModifier modifier = ModifierRegistry.fromBytes(buffer);

            if (modifier != null)
            {
                this.modifiers.add(modifier);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.modifiers.size());

        for (AbstractModifier modifier : this.modifiers)
        {
            ModifierRegistry.toBytes(modifier, buffer);
        }
    }
}
