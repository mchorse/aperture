package mchorse.aperture.camera.values;

import mchorse.aperture.camera.smooth.Envelope;
import mchorse.mclib.config.values.Value;

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

        this.assign(envelope);
    }

    private void assign(Envelope envelope)
    {
        this.envelope = envelope;

        this.removeAllSubValues();

        if (envelope != null)
        {
            for (Value value : envelope.getProperties())
            {
                this.addSubValue(value);
            }
        }
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
        this.assign(new Envelope());
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueEnvelope)
        {
            this.set(((ValueEnvelope) value).get());
        }
    }
}
