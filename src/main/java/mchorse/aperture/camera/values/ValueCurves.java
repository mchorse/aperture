package mchorse.aperture.camera.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueCurves extends Value
{
    private Map<String, KeyframeChannel> curves = new HashMap<String, KeyframeChannel>();

    public ValueCurves(String id)
    {
        super(id);
    }

    public Map<String, KeyframeChannel> get()
    {
        return this.curves;
    }

    public void set(Map<String, KeyframeChannel> curves)
    {
        this.curves.clear();

        for (Map.Entry<String, KeyframeChannel> entry : curves.entrySet())
        {
            KeyframeChannel newCurve = new KeyframeChannel();

            newCurve.copy(entry.getValue());
            this.curves.put(entry.getKey(), newCurve);
        }
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        List<IConfigValue> values = new ArrayList<IConfigValue>();

        for (Map.Entry<String, KeyframeChannel> entry : curves.entrySet())
        {
            values.add(new ValueKeyframeChannel(this.getId() + "." + entry.getKey(), entry.getValue()));
        }

        return values;
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
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValueCurves)
        {
            this.set(((ValueCurves) value).get());
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

        for (Map.Entry<String, JsonElement> entry : object.entrySet())
        {
            if (!entry.getValue().isJsonArray())
            {
                continue;
            }

            JsonArray curve = entry.getValue().getAsJsonArray();
            KeyframeChannel channel = new KeyframeChannel();

            channel.fromJSON(curve);
            this.curves.put(entry.getKey(), channel);
        }
    }

    @Override
    public JsonElement toJSON()
    {
        JsonObject object = new JsonObject();

        for (Map.Entry<String, KeyframeChannel> entry : this.curves.entrySet())
        {
            object.add(entry.getKey(), entry.getValue().toJSON());
        }

        return object;
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.curves.clear();

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            String key = ByteBufUtils.readUTF8String(buffer);
            KeyframeChannel curve = new KeyframeChannel();

            curve.fromBytes(buffer);
            this.curves.put(key, curve);
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.curves.size());

        for (Map.Entry<String, KeyframeChannel> entry : this.curves.entrySet())
        {
            ByteBufUtils.writeUTF8String(buffer, entry.getKey());
            entry.getValue().toBytes(buffer);
        }
    }
}
