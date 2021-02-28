package mchorse.aperture.camera.json;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;

public class ModifierSerializer
{
    /**
     * Read an abstract modifier from a byte buffer
     */
    public static AbstractModifier fromBytes(ByteBuf buffer)
    {
        try
        {
            AbstractModifier modifier = ModifierRegistry.fromType(buffer.readByte());

            modifier.fromBytes(buffer);

            return modifier;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Write an abstract modifier to byte buffer
     */
    public static void toBytes(AbstractModifier modifier, ByteBuf buffer)
    {
        buffer.writeByte(ModifierRegistry.getType(modifier));

        modifier.toBytes(buffer);
    }

    /**
     * Create modifier from JSON
     */
    public static AbstractModifier fromJSON(JsonObject object)
    {
        String type = object.has("type") && object.get("type").isJsonPrimitive() ? object.get("type").getAsString() : "";
        Class<? extends AbstractModifier> clazz = ModifierRegistry.NAME_TO_CLASS.get(type);

        if (clazz != null)
        {
            try
            {
                AbstractModifier modifier = clazz.getConstructor().newInstance();

                modifier.fromJSON(object);

                return modifier;
            }
            catch (Exception e)
            {}
        }

        return null;
    }

    /**
     * Seriealize given modifier to JSON
     */
    public static JsonObject toJSON(AbstractModifier modifier)
    {
        JsonObject object = new JsonObject();

        object.addProperty("type", ModifierRegistry.NAME_TO_CLASS.inverse().get(modifier.getClass()));
        modifier.toJSON(object);

        return object;
    }
}
