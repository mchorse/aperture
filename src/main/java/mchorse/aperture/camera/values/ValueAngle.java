package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Angle;
import mchorse.mclib.config.values.Value;

public class ValueAngle extends Value
{
    private Angle angle;

    public ValueAngle(String id, Angle angle)
    {
        super(id);

        this.angle = angle;
    }

    public Angle get()
    {
        return this.angle;
    }

    public void set(Angle point)
    {
        this.angle.set(point);
    }

    @Override
    public Object getValue()
    {
        return this.angle.copy();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof Angle)
        {
            this.angle.set((Angle) object);
        }
    }

    @Override
    public void reset()
    {
        this.angle.set(0, 0, 0, 70);
    }

    @Override
    public void valueFromJSON(JsonElement element)
    {
        this.angle.fromJSON(element.getAsJsonObject());
    }

    @Override
    public JsonElement valueToJSON()
    {
        return this.angle.toJSON();
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueAngle)
        {
            this.set(((ValueAngle) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.angle.set(Angle.fromBytes(buffer));
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        this.angle.toBytes(buffer);
    }
}
