package mchorse.aperture.camera.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.mclib.utils.keyframes.KeyframeChannel;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Camera profile JSON adapter
 *
 * This bad boy is needed only to discard fixtures/modifiers that had an exception when parsing
 * or serializing them...
 */
public class CameraProfileAdapter implements JsonDeserializer<CameraProfile>, JsonSerializer<CameraProfile>
{
	@Override
	public CameraProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		CameraProfile profile = new CameraProfile(null);

		if (!json.isJsonObject())
		{
			return profile;
		}

		JsonObject object = json.getAsJsonObject();

		if (object.has("fixtures") && object.get("fixtures").isJsonArray())
		{
			for (JsonElement element : object.get("fixtures").getAsJsonArray())
			{
				AbstractFixture fixture = context.deserialize(element, AbstractFixture.class);

				if (fixture != null)
				{
					profile.getAll().add(fixture);
				}
			}
		}

		if (object.has("modifiers") && object.get("modifiers").isJsonArray())
		{
			for (JsonElement element : object.get("modifiers").getAsJsonArray())
			{
				AbstractModifier modifier = context.deserialize(element, AbstractModifier.class);

				if (modifier != null)
				{
					profile.getModifiers().add(modifier);
				}
			}
		}

		if (object.has("curves") && object.get("curves").isJsonObject())
		{
			JsonObject curves = object.get("curves").getAsJsonObject();

			for (Map.Entry<String, JsonElement> entry : curves.entrySet())
			{
				KeyframeChannel channel = context.deserialize(entry.getValue(), KeyframeChannel.class);

				if (channel != null)
				{
					profile.getCurves().put(entry.getKey(), channel);
				}
			}
		}

		return profile;
	}

	@Override
	public JsonElement serialize(CameraProfile src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();
		JsonArray fixtures = new JsonArray();
		JsonArray modifiers = new JsonArray();
		JsonObject curves = new JsonObject();

		object.add("fixtures", fixtures);
		object.add("modifiers", modifiers);
		object.add("curves", curves);

		for (AbstractFixture fixture : src.getAll())
		{
			JsonElement element = context.serialize(fixture, AbstractFixture.class);

			if (element != null)
			{
				fixtures.add(element);
			}
		}

		for (AbstractModifier modifier : src.getModifiers())
		{
			JsonElement element = context.serialize(modifier, AbstractModifier.class);

			if (element != null)
			{
				modifiers.add(element);
			}
		}

		for (Map.Entry<String, KeyframeChannel> entry : src.getCurves().entrySet())
		{
			curves.add(entry.getKey(), context.serialize(entry.getValue()));
		}

		return object;
	}
}