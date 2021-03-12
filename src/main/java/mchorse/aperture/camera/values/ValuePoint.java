package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Point;
import mchorse.mclib.config.values.Value;

public class ValuePoint extends Value
{
    private Point point;

    public ValuePoint(String id, Point Point)
    {
        super(id);

        this.point = Point;
    }

    public Point get()
    {
        return this.point;
    }

    public void set(Point point)
    {
        this.point.set(point);
    }

    @Override
    public Object getValue()
    {
        return this.point.copy();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof Point)
        {
            this.point.set((Point) object);
        }
    }

    @Override
    public void reset()
    {
        this.point.set(0, 0, 0);
    }

    @Override
    public void valueFromJSON(JsonElement element)
    {
        this.point.fromJSON(element.getAsJsonObject());
    }

    @Override
    public JsonElement valueToJSON()
    {
        return this.point.toJSON();
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValuePoint)
        {
            this.set(((ValuePoint) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.point.set(Point.fromBytes(buffer));
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.point.toBytes(buffer);
    }
}
