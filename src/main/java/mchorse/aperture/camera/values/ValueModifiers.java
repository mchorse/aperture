package mchorse.aperture.camera.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.json.ModifierSerializer;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValueModifiers extends Value
{
    private List<AbstractModifier> modifiers = new ArrayList<AbstractModifier>();

    public ValueModifiers(String id)
    {
        super(id);
    }

    public void add(AbstractModifier modifier)
    {
        this.modifiers.add(modifier);
        this.addSubValue(new ValueModifier(String.valueOf(this.modifiers.size() - 1), modifier));
    }

    public void add(int index, AbstractModifier modifier)
    {
        this.modifiers.add(index, modifier);
        this.sync();
    }

    public AbstractModifier remove(int index)
    {
        AbstractModifier modifier = this.modifiers.remove(index);

        this.sync();

        return modifier;
    }

    public AbstractModifier get(int index)
    {
        return this.modifiers.get(index);
    }

    public int size()
    {
        return this.modifiers.size();
    }

    public int indexOf(AbstractModifier modifier)
    {
        return this.modifiers.indexOf(modifier);
    }

    public void sync()
    {
        this.removeAllSubValues();

        int i = 0;

        for (AbstractModifier modifier : this.modifiers)
        {
            this.addSubValue(new ValueModifier(String.valueOf(i), modifier));

            i += 1;
        }
    }

    /* public List<AbstractModifier> get()
    {
        return this.modifiers;
    } */

    public void set(List<AbstractModifier> modifiers)
    {
        this.reset();

        for (AbstractModifier modifier : modifiers)
        {
            this.add(modifier.copy());
        }
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
        this.removeAllSubValues();
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueModifiers)
        {
            this.set(((ValueModifiers) value).modifiers);
        }
    }

    @Override
    public void valueFromJSON(JsonElement element)
    {
        if (!element.isJsonArray())
        {
            return;
        }

        JsonArray array = element.getAsJsonArray();

        this.reset();

        for (JsonElement jsonElement : array)
        {
            if (!jsonElement.isJsonObject())
            {
                continue;
            }

            JsonObject object = jsonElement.getAsJsonObject();
            AbstractModifier modifier = ModifierSerializer.fromJSON(object);

            if (modifier != null)
            {
                this.add(modifier);
            }
        }
    }

    @Override
    public JsonElement valueToJSON()
    {
        JsonArray array = new JsonArray();

        for (AbstractModifier modifier : this.modifiers)
        {
            array.add(ModifierSerializer.toJSON(modifier));
        }

        return array;
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.reset();

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractModifier modifier = ModifierSerializer.fromBytes(buffer);

            if (modifier != null)
            {
                this.add(modifier);
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
            ModifierSerializer.toBytes(modifier, buffer);
        }
    }
}
