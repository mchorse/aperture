package mchorse.aperture.camera.json;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;

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
            Class<? extends AbstractFixture> clazz = FixtureRegistry.NAME_TO_CLASS.get(type);

            if (clazz != null)
            {
                fixture = this.gson.fromJson(json, clazz);
                fixture.fromJSON(object);
            }
        }

        return fixture;
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