package mchorse.aperture.camera.json;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.modifiers.RemapperModifier;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeChannel;

/**
 * This class is responsible for serializing and deserializing 
 * registered camera fixtures to JSON.
 */
public class AbstractFixtureAdapter implements JsonSerializer<AbstractFixture>, JsonDeserializer<AbstractFixture>
{
    /**
     * Gson instance for building up
     */
    private Gson gson;

    public AbstractFixtureAdapter()
    {
        GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();

        builder.registerTypeAdapter(AbstractModifier.class, new AbstractModifierAdapter());

        this.gson = builder.create();
    }

    /**
     * Deserialize an abstract fixture from JsonElement.
     *
     * This method extracts type from the JSON map, and creates a fixture
     * from the type (which is mapped to a class). It also responsible for
     * setting the target for follow and look fixtures.
     */
    @Override
    public AbstractFixture deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();
        AbstractFixture fixture = null;

        if (object.has("type"))
        {
            String type = object.get("type").getAsString();

            /* Special case for old per-point duration path */
            Class<? extends AbstractFixture> clazz = FixtureRegistry.NAME_TO_CLASS.get(type);

            if (clazz != null)
            {
                try
                {
                    fixture = clazz.getConstructor(long.class).newInstance(0);
                    fixture.fromJSON(object);
                }
                catch (Exception e)
                {}
            }

            /* Special case for per-point path */
            if (type.equals("path") && fixture != null)
            {
                if (object.has("perPointDuration") && object.get("perPointDuration").getAsBoolean())
                {
                    return this.convertToKeyframe((PathFixture) fixture, object.getAsJsonArray("points"));
                }
                else if (object.has("useFactor"))
                {
                    JsonElement useFactor = object.get("useFactor");

                    if (useFactor.isJsonPrimitive() && useFactor.getAsBoolean())
                    {
                        this.convertUseFactorToRemapper((PathFixture) fixture, object);
                    }
                }
            }
        }

        return fixture;
    }

    /**
     * Since I removed per-point duration from path fixture, I suppose I should
     * add a feature to not completely lose that data...
     */
    private AbstractFixture convertToKeyframe(PathFixture fixture, JsonArray points)
    {
        if (points.size() != fixture.getCount())
        {
            return fixture;
        }

        KeyframeFixture keys = fixture.toKeyframe();
        int x = 0;

        for (int i = 0, c = fixture.getCount(); i < c; i++)
        {
            JsonObject point = points.get(i).getAsJsonObject();

            for (KeyframeChannel channel : keys.channels)
            {
                Keyframe frame = channel.get(i);

                frame.tick = x;
            }

            if (i == c - 1)
            {
                continue;
            }

            if (point.has("duration"))
            {
                x += point.get("duration").getAsInt();
            }
            else
            {
                x += 1;
            }
        }

        keys.setDuration(x);

        return keys;
    }

    /**
     * Convert use factor option to a remapper modifier
     */
    private void convertUseFactorToRemapper(PathFixture fixture, JsonObject object)
    {
        RemapperModifier modifier = new RemapperModifier();

        modifier.keyframes = true;
        modifier.channel.copy(fixture.speed);

        fixture.useSpeed = false;
        fixture.getModifiers().add(0, modifier);
    }

    /**
     * Serialize an abstract fixture into JsonElement.
     *
     * This method also responsible for giving the serialized abstract fixture
     * a type key (for later ability to deserialize exact type of the JSON
     * element) and target key for look and follow fixtures.
     */
    @Override
    public JsonElement serialize(AbstractFixture src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = (JsonObject) this.gson.toJsonTree(src);

        object.addProperty("type", FixtureRegistry.NAME_TO_CLASS.inverse().get(src.getClass()));
        src.toJSON(object);

        return object;
    }
}