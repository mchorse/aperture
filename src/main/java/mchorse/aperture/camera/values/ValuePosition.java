package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.config.values.Value;

public class ValuePosition extends Value
{
    private Position position;
    private ValuePoint pointDelegate;
    private ValueAngle angleDelegate;

    public ValuePosition(String id)
    {
        this(id, new Position());
    }

    public ValuePosition(String id, Position position)
    {
        super(id);

        this.position = position;
        this.pointDelegate = new ValuePoint("point", this.position.point);
        this.angleDelegate = new ValueAngle("angle", this.position.angle);

        this.addSubValue(this.pointDelegate);
        this.addSubValue(this.angleDelegate);
    }

    public ValuePoint getPoint()
    {
        return this.pointDelegate;
    }

    public ValueAngle getAngle()
    {
        return this.angleDelegate;
    }

    public Position get()
    {
        return this.position;
    }

    public void set(Position position)
    {
        this.position.set(position);
        this.saveLater();
    }

    @Override
    public Object getValue()
    {
        return this.position.copy();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof Position)
        {
            this.set((Position) object);
        }
    }

    @Override
    public void reset()
    {
        this.position.set(new Position());
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        if (element.isJsonObject())
        {
            this.position.fromJSON((JsonObject) element);
        }
    }

    @Override
    public JsonElement toJSON()
    {
        return this.position.toJSON();
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValuePosition)
        {
            this.set(((ValuePosition) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.position.set(Position.fromBytes(buffer));
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        this.position.toBytes(buffer);
    }
}
