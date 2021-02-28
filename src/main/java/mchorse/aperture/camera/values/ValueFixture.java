package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.json.FixtureSerializer;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValueFixture extends Value
{
    private AbstractFixture fixture;

    public ValueFixture(String id, AbstractFixture fixture)
    {
        super(id);

        this.fixture = fixture;
    }

    public AbstractFixture get()
    {
        return this.fixture;
    }

    public void set(AbstractFixture fixture)
    {
        if (fixture != null)
        {
            this.fixture = fixture.copy();
        }
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        List<IConfigValue> values = new ArrayList<IConfigValue>();

        for (IConfigValue value : this.fixture.getProperties())
        {
            values.add(new ValueProxy(this.getId() + "." + value.getId(), value));
        }

        return values;
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
        this.fixture = null;
    }

    @Override
    public void copy(IConfigValue value)
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

        if (fixture != null)
        {
            this.fixture = fixture;
        }
    }

    @Override
    public JsonElement toJSON()
    {
        return FixtureSerializer.toJSON(this.fixture);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.fixture = FixtureSerializer.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        FixtureSerializer.toBytes(this.fixture, buffer);
    }
}
