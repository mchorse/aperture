package mchorse.aperture.camera.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import mchorse.aperture.camera.fixtures.ManualFixture;

import java.lang.reflect.Type;

public class RenderFrameAdapter implements JsonDeserializer<ManualFixture.RenderFrame>, JsonSerializer<ManualFixture.RenderFrame>
{
    @Override
    public ManualFixture.RenderFrame deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        ManualFixture.RenderFrame frame = new ManualFixture.RenderFrame();

        if (json.isJsonArray())
        {
            JsonArray array = json.getAsJsonArray();

            frame.x = array.get(0).getAsDouble();
            frame.y = array.get(1).getAsDouble();
            frame.z = array.get(2).getAsDouble();
            frame.yaw = array.get(3).getAsFloat();
            frame.pitch = array.get(4).getAsFloat();
            frame.roll = array.get(5).getAsFloat();
            frame.fov = array.get(6).getAsFloat();
        }

        return frame;
    }

    @Override
    public JsonElement serialize(ManualFixture.RenderFrame src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonArray array = new JsonArray();

        array.add(src.x);
        array.add(src.y);
        array.add(src.z);
        array.add(src.yaw);
        array.add(src.pitch);
        array.add(src.roll);
        array.add(src.fov);

        return array;
    }
}
