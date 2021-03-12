package mchorse.aperture.camera.values;

import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.keyframes.KeyframeChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValueCurves extends Value
{
    private Map<String, KeyframeChannel> curves = new HashMap<String, KeyframeChannel>();

    public ValueCurves(String id)
    {
        super(id);
    }

    public KeyframeChannel get(String key)
    {
        return this.curves.get(key);
    }

    public Set<String> keys()
    {
        return this.curves.keySet();
    }

    public void put(String key, KeyframeChannel channel)
    {
        boolean contained = this.curves.containsKey(key);

        this.curves.put(key, channel);

        if (contained)
        {
            ((ValueKeyframeChannel) this.getSubValue(key)).set(channel);
        }
        else
        {
            this.addSubValue(new ValueKeyframeChannel(key, channel).setParent(this));
        }
    }

    public void set(Map<String, KeyframeChannel> curves)
    {
        this.reset();

        for (Map.Entry<String, KeyframeChannel> entry : curves.entrySet())
        {
            KeyframeChannel newCurve = new KeyframeChannel();

            newCurve.copy(entry.getValue());
            this.put(entry.getKey(), newCurve);
        }
    }

    @Override
    public Object getValue()
    {
        Map<String, KeyframeChannel> curves = new HashMap<String, KeyframeChannel>();

        for (Map.Entry<String, KeyframeChannel> entry : curves.entrySet())
        {
            KeyframeChannel newCurve = new KeyframeChannel();

            newCurve.copy(entry.getValue());
            curves.put(entry.getKey(), newCurve);
        }

        return curves;
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof Map)
        {
            Map map = (Map) object;

            if (map.isEmpty() || map.get(map.keySet().iterator().next()) instanceof KeyframeChannel)
            {
                this.set((Map<String, KeyframeChannel>) map);
            }
        }
    }

    @Override
    public void reset()
    {
        this.curves.clear();
        this.removeAllSubValues();
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueCurves)
        {
            this.set(((ValueCurves) value).curves);
        }
    }
}
