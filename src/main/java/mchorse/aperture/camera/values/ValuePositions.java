package mchorse.aperture.camera.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValuePositions extends Value
{
    private List<Position> positions = new ArrayList<Position>();

    public ValuePositions(String id)
    {
        super(id);
    }

    public void add(Position position)
    {
        this.positions.add(position);
        this.addSubValue(new ValuePosition(String.valueOf(this.positions.size() - 1), position));
    }

    public Position get(int index)
    {
        return this.positions.get(index);
}

    public int size()
    {
        return this.positions.size();
    }

    public void sync()
    {
        this.removeAllSubValues();

        int i = 0;

        for (Position position : this.positions)
        {
            this.addSubValue(new ValuePosition(String.valueOf(i), position));

            i += 1;
        }
    }

    public void set(List<Position> positions)
    {
        this.reset();

        for (Position position : positions)
        {
            this.add(position.copy());
        }
    }

    @Override
    public Object getValue()
    {
        List<Position> positions = new ArrayList<Position>();

        for (Position position : this.positions)
        {
            positions.add(position.copy());
        }

        return positions;
    }

    @Override
    public void setValue(Object value)
    {
        if (value instanceof List)
        {
            List list = (List) value;

            if (list.isEmpty() || list.get(0) instanceof Position)
            {
                this.set((List<Position>) list);
            }
        }
    }

    @Override
    public void reset()
    {
        this.positions.clear();
        this.removeAllSubValues();
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        if (element.isJsonArray())
        {
            this.reset();

            JsonArray array = element.getAsJsonArray();

            for (JsonElement child : array)
            {
                if (child.isJsonObject())
                {
                    Position position = new Position();

                    position.fromJSON(child.getAsJsonObject());
                    this.add(position);
                }
            }
        }
    }

    @Override
    public JsonElement toJSON()
    {
        JsonArray array = new JsonArray();

        for (Position position : this.positions)
        {
            array.add(position.toJSON());
        }

        return array;
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValuePositions)
        {
            this.set(((ValuePositions) value).positions);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.reset();

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            this.add(Position.fromBytes(buffer));
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(this.positions.size());

        for (Position position : this.positions)
        {
            position.toBytes(buffer);
        }
    }
}