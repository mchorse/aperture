package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.InterpolationType;
import mchorse.mclib.config.values.Value;

public class ValueInterpolationType extends Value
{
    private InterpolationType interp = InterpolationType.HERMITE;

    public ValueInterpolationType(String id)
    {
        super(id);
    }

    public InterpolationType get()
    {
        return this.interp;
    }

    public void set(InterpolationType interp)
    {
        this.interp = interp;
        this.saveLater();
    }

    @Override
    public Object getValue()
    {
        return this.interp;
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof InterpolationType)
        {
            this.set((InterpolationType) object);
        }
    }

    @Override
    public void reset()
    {
        this.interp = InterpolationType.HERMITE;
    }

    @Override
    public void valueFromJSON(JsonElement element)
    {
        for (InterpolationType type : InterpolationType.values())
        {
            if (type.name.equals(element.getAsString()))
            {
                this.interp = type;

                break;
            }
        }
    }

    @Override
    public JsonElement valueToJSON()
    {
        return new JsonPrimitive(this.interp.name);
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueInterpolationType)
        {
            this.set(((ValueInterpolationType) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.interp = InterpolationType.values()[buffer.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.interp.ordinal());
    }
}