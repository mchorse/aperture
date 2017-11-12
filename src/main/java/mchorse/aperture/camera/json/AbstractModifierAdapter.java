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

import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;

/**
 * Abstract modifier adapter
 * 
 * This class is responsible for serializing and deserializing
 * registered camera modifiers to JSON.
 */
public class AbstractModifierAdapter implements JsonSerializer<AbstractModifier>, JsonDeserializer<AbstractModifier>
{
    /**
     * Gson instance for building up JSON objects
     */
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Deserialize a camera modifier
     * 
     * The deserializing is really depends on the type, so without 
     * supplied type, it's impossible to construct a camera modifier.
     */
    @Override
    public AbstractModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();
        AbstractModifier modifier = null;

        if (object.has("type"))
        {
            String type = object.get("type").getAsString();

            modifier = this.gson.fromJson(json, ModifierRegistry.NAME_TO_CLASS.get(type));
            modifier.fromJSON(object);
        }

        return modifier;
    }

    /**
     * Serialize a camera modifier
     * 
     * Serializing is way easier than deserializing, since we've got all 
     * needed data, so we're just assigning additional "type" field 
     * which will be used for deserializing. 
     */
    @Override
    public JsonElement serialize(AbstractModifier src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = (JsonObject) this.gson.toJsonTree(src);

        object.addProperty("type", ModifierRegistry.NAME_TO_CLASS.inverse().get(src.getClass()));
        src.toJSON(object);

        return object;
    }
}