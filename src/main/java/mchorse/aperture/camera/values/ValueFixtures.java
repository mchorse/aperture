package mchorse.aperture.camera.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.json.FixtureSerializer;
import mchorse.mclib.config.values.IConfigValue;
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

    public List<AbstractFixture> get()
    {
        return this.fixtures;
    }

    public void set(List<AbstractFixture> fixtures)
    {
        this.fixtures.clear();

        for (AbstractFixture fixture : fixtures)
        {
            this.fixtures.add(fixture.copy());
        }
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        List<IConfigValue> values = new ArrayList<IConfigValue>();
        int i = 0;

        for (AbstractFixture fixture : this.fixtures)
        {
            values.add(new ValueFixture(this.getId() + "." + i, fixture));

            i += 1;
        }

        return values;
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
    }

    @Override
    public void copy(IConfigValue value)
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

        this.fixtures.clear();

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
                this.fixtures.add(fixture);
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
        super.fromBytes(buffer);

        this.fixtures.clear();

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractFixture fixture = FixtureSerializer.fromBytes(buffer);

            if (fixture != null)
            {
                this.fixtures.add(fixture);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.fixtures.size());

        for (AbstractFixture fixture : this.fixtures)
        {
            FixtureSerializer.toBytes(fixture, buffer);
        }
    }
}