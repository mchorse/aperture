package mchorse.aperture.camera.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.camera.modifiers.ShakeModifier;

public class AbstractModifierAdapter implements JsonSerializer<AbstractModifier>, JsonDeserializer<AbstractModifier>
{
    /**
     * Gson instance for building up
     */
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Map<String, Class<? extends AbstractModifier>> TYPES = new HashMap<String, Class<? extends AbstractModifier>>();

    static
    {
        TYPES.put("shake", ShakeModifier.class);
        TYPES.put("math", MathModifier.class);
    }

    @Override
    public AbstractModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();
        AbstractModifier modifier = null;

        if (object.has("type"))
        {
            modifier = this.gson.fromJson(json, TYPES.get(object.get("type").getAsString()));
            modifier.fromJSON(object);
        }

        return modifier;
    }

    @Override
    public JsonElement serialize(AbstractModifier src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = (JsonObject) this.gson.toJsonTree(src);

        src.toJSON(object);
        object.addProperty("type", AbstractFixtureAdapter.getKeyByValue(TYPES, src.getClass()));

        return object;
    }
}