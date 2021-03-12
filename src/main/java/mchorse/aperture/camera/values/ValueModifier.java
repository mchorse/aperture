package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.json.ModifierSerializer;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValueModifier extends Value
{
    private AbstractModifier modifier;

    public ValueModifier(String id, AbstractModifier modifier)
    {
        super(id);

        this.assign(modifier);
    }

    public void assign(AbstractModifier modifier)
    {
        this.modifier = modifier;

        this.removeAllSubValues();

        if (modifier != null)
        {
            for (Value value : modifier.getProperties())
            {
                this.addSubValue(value);
            }
        }
    }

    public AbstractModifier get()
    {
        return this.modifier;
    }

    public void set(AbstractModifier modifier)
    {
        if (modifier != null)
        {
            this.assign(modifier.copy());
        }
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
        this.assign(null);
    }

    @Override
    public void copy(Value value)
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
        AbstractModifier modifier = ModifierSerializer.fromJSON(object);

        if (modifier != null)
        {
            this.assign(modifier);
        }
    }

    @Override
    public JsonElement toJSON()
    {
        return ModifierSerializer.toJSON(this.modifier);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.assign(ModifierSerializer.fromBytes(buffer));
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        ModifierSerializer.toBytes(this.modifier, buffer);
    }
}
