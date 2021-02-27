package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.smooth.Envelope;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValueEnvelope extends Value
{
    private Envelope envelope;

    public ValueEnvelope(String id)
    {
        this(id, new Envelope());
    }

    public ValueEnvelope(String id, Envelope envelope)
    {
        super(id);

        this.envelope = envelope;
    }

    public Envelope get()
    {
        return this.envelope;
    }

    public void set(Envelope envelope)
    {
        this.envelope.copy(envelope);
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        return new ArrayList<IConfigValue>(this.envelope.getProperties());
    }

    @Override
    public Object getValue()
    {
        return this.envelope.copy();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof Envelope)
        {
            this.set((Envelope) object);
        }
    }

    @Override
    public void reset()
    {
        this.envelope = new Envelope();
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValueEnvelope)
        {
            this.set(((ValueEnvelope) value).get());
        }
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        if (!element.isJsonObject())
        {
            return;
        }

        this.envelope = new Envelope();
        this.envelope.fromJSON(element.getAsJsonObject());
    }

    @Override
    public JsonElement toJSON()
    {
        JsonObject object = new JsonObject();

        this.envelope.toJSON(object);

        return object;
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.envelope.toBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.envelope.fromBytes(buffer);
    }
}
