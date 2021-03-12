package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.json.FixtureSerializer;
import mchorse.mclib.config.values.Value;

public class ValueFixture extends Value
{
    private AbstractFixture fixture;

    public ValueFixture(String id, AbstractFixture fixture)
    {
        super(id);

        this.assign(fixture);
    }

    public void assign(AbstractFixture fixture)
    {
        this.fixture = fixture;

        this.removeAllSubValues();

        if (fixture != null)
        {
            for (Value value : fixture.getProperties())
            {
                this.addSubValue(value);
            }
        }
    }

    public AbstractFixture get()
    {
        return this.fixture;
    }

    public void set(AbstractFixture fixture)
    {
        if (fixture != null)
        {
            this.assign(fixture.copy());
        }
    }

    @Override
    public Object getValue()
    {
        return this.fixture.copy();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof AbstractFixture)
        {
            this.set((AbstractFixture) object);
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
        if (value instanceof ValueFixture)
        {
            this.setValue(((ValueFixture) value).get());
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
        AbstractFixture fixture = FixtureSerializer.fromJSON(object);

        this.assign(fixture);
    }

    @Override
    public JsonElement toJSON()
    {
        return FixtureSerializer.toJSON(this.fixture);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.assign(FixtureSerializer.fromBytes(buffer));
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        FixtureSerializer.toBytes(this.fixture, buffer);
    }
}
