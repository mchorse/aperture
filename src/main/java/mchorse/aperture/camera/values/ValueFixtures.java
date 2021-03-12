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
    private List<AbstractFixture> fixtures = new ArrayList<AbstractFixture>();

    public ValueFixtures(String id)
    {
        super(id);
    }

    public void add(AbstractFixture fixture)
    {
        this.fixtures.add(fixture);
        this.addSubValue(new ValueFixture(String.valueOf(this.fixtures.size() - 1), fixture));
    }

    public void add(int index, AbstractFixture fixture)
    {
        this.fixtures.add(index, fixture);
        this.sync();
    }

    public AbstractFixture remove(int index)
    {
        AbstractFixture fixture = this.fixtures.remove(index);

        this.sync();

        return fixture;
    }

    public AbstractFixture get(int index)
    {
        return this.fixtures.get(index);
    }

    public int size()
    {
        return this.fixtures.size();
    }

    public int indexOf(AbstractFixture fixture)
    {
        return this.fixtures.indexOf(fixture);
    }

    public void sync()
    {
        this.removeAllSubValues();

        int i = 0;

        for (AbstractFixture fixture : this.fixtures)
        {
            this.addSubValue(new ValueFixture(String.valueOf(i), fixture));

            i += 1;
        }
    }

    public List<AbstractFixture> get()
    {
        return this.fixtures;
    }

    public void set(List<AbstractFixture> fixtures)
    {
        this.reset();

        for (AbstractFixture fixture : fixtures)
        {
            this.add(fixture.copy());
        }
    }

    @Override
    public Object getValue()
    {
        List<AbstractFixture> fixtures = new ArrayList<AbstractFixture>();

        for (AbstractFixture fixture : this.fixtures)
        {
            fixtures.add(fixture.copy());
        }

        return fixtures;
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof List)
        {
            List list = (List) object;

            if (list.isEmpty() || list.get(0) instanceof AbstractFixture)
            {
                this.set((List<AbstractFixture>) list);
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
            this.set(((ValueFixtures) value).get());
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

        for (AbstractFixture fixture : this.fixtures)
        {
            array.add(FixtureSerializer.toJSON(fixture));
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

        for (AbstractFixture fixture : this.fixtures)
        {
            FixtureSerializer.toBytes(fixture, buffer);
        }
    }
}