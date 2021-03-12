package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.Interpolation;

public class ValueInterpolation extends Value
{
    private Interpolation interp = Interpolation.LINEAR;

    public ValueInterpolation(String id)
    {
        super(id);
    }

    public Interpolation get()
    {
        return this.interp;
    }

    public void set(Interpolation interp)
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
        if (object instanceof Interpolation)
        {
            this.set((Interpolation) object);
        }
    }

    @Override
    public void reset()
    {
        this.interp = Interpolation.LINEAR;
    }

    @Override
    public void valueFromJSON(JsonElement element)
    {
        this.interp = Interpolation.valueOf(element.getAsString());
    }

    @Override
    public JsonElement valueToJSON()
    {
        return new JsonPrimitive(this.interp.toString());
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueInterpolation)
        {
            this.set(((ValueInterpolation) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.interp = Interpolation.values()[buffer.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.interp.ordinal());
    }
}