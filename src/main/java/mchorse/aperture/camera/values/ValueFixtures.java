package mchorse.aperture.camera.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.json.FixtureSerializer;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValueFixtures extends Value
{
    private List<ValueFixture> fixtures = new ArrayList<ValueFixture>();

    public ValueFixtures(String id)
    {
        super(id);
    }

    public void add(AbstractFixture fixture)
    {
        ValueFixture value = new ValueFixture(String.valueOf(this.fixtures.size()), fixture);

        this.fixtures.add(value);
        this.addSubValue(value);
    }

    public void add(int index, AbstractFixture fixture)
    {
        this.fixtures.add(index, new ValueFixture("", fixture));
        this.sync();
    }

    public AbstractFixture remove(int index)
    {
        ValueFixture fixture = this.fixtures.remove(index);

        this.sync();

        return fixture.get();
    }

    public AbstractFixture get(int index)
    {
        return this.fixtures.get(index).get();
    }

    public int size()
    {
        return this.fixtures.size();
    }

    public int indexOf(AbstractFixture fixture)
    {
        for (int i = 0; i < this.fixtures.size(); i++)
        {
            if (fixture == this.fixtures.get(i).get())
            {
                return i;
            }
        }

        return -1;
    }

    public void sync()
    {
        List<ValueFixture> fixtures = new ArrayList<ValueFixture>();

        this.removeAllSubValues();

        for (int i = 0; i < this.fixtures.size(); i++)
        {
            ValueFixture fixture = new ValueFixture(String.valueOf(i), this.fixtures.get(i).get());

            this.addSubValue(fixture);
            fixtures.add(fixture);
        }

        this.fixtures = fixtures;
    }

    public List<ValueFixture> get()
    {
        return this.fixtures;
    }

    public void set(List<ValueFixture> fixtures)
    {
        this.reset();

        for (ValueFixture fixture : fixtures)
        {
            this.add(fixture.get().copy());
        }
    }

    @Override
    public Object getValue()
    {
        List<AbstractFixture> fixtures = new ArrayList<AbstractFixture>();

        for (ValueFixture fixture : this.fixtures)
        {
            fixtures.add(fixture.get().copy());
        }

        return fixtures;
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof List)
        {
            List list = (List) object;

            if (list.isEmpty() || list.get(0) instanceof ValueFixture)
            {
                this.set((List<ValueFixture>) list);
            }
        }
    }

    @Override
    public void reset()
    {
        this.fixtures.clear();
        this.removeAllSubValues();
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueFixtures)
        {
            this.set(((ValueFixtures) value).fixtures);
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

        this.reset();

        for (JsonElement jsonElement : array)
        {
            if (!jsonElement.isJsonObject())
            {
                continue;
            }

            JsonObject object = jsonElement.getAsJsonObject();
            AbstractFixture fixture = FixtureSerializer.fromJSON(object);

            if (fixture != null)
            {
                this.add(fixture);
            }
        }
    }

    @Override
    public JsonElement toJSON()
    {
        JsonArray array = new JsonArray();

        for (ValueFixture fixture : this.fixtures)
        {
            array.add(FixtureSerializer.toJSON(fixture.get()));
        }

        return array;
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.reset();

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractFixture fixture = FixtureSerializer.fromBytes(buffer);

            if (fixture != null)
            {
                this.add(fixture);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(this.fixtures.size());

        for (ValueFixture fixture : this.fixtures)
        {
            FixtureSerializer.toBytes(fixture.get(), buffer);
        }
    }
}