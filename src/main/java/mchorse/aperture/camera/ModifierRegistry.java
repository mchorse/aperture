package mchorse.aperture.camera;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.mclib.utils.Color;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

/**
 * Modifier registry
 * 
 * That's the place where you register abstract modifiers
 */
public class ModifierRegistry
{
    /**
     * Bi-directional map between class and byte ID
     */
    public static final BiMap<Class<? extends AbstractModifier>, Byte> CLASS_TO_ID = HashBiMap.create();

    /**
     * Registered modifier name mapped to a class. 
     */
    public static final BiMap<String, Class<? extends AbstractModifier>> NAME_TO_CLASS = HashBiMap.create();

    /**
     * Client information about camera modifier 
     */
    @SideOnly(Side.CLIENT)
    public static Map<Class<? extends AbstractModifier>, ModifierInfo> CLIENT;

    /**
     * Next available id 
     */
    private static byte NEXT_ID = 0;

    public static byte getNextId()
    {
        return NEXT_ID;
    }

    /**
     * Get type from abstract modifier
     */
    public static byte getType(AbstractModifier modifier)
    {
        Byte type = CLASS_TO_ID.get(modifier.getClass());

        return type == null ? -1 : type.byteValue();
    }

    /**
     * Create an abstract modifier from given byte  
     */
    public static AbstractModifier fromType(byte type) throws Exception
    {
        Class<? extends AbstractModifier> clazz = CLASS_TO_ID.inverse().get(type);

        if (clazz != null)
        {
            return clazz.getConstructor().newInstance();
        }

        throw new Exception("Modifier with type '" + type + "' not exists!");
    }

    /**
     * Read an abstract modifier from a byte buffer
     */
    public static AbstractModifier fromBytes(ByteBuf buffer)
    {
        try
        {
            AbstractModifier modifier = fromType(buffer.readByte());

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
        buffer.writeByte(getType(modifier));

        modifier.toBytes(buffer);
    }

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

    public static JsonObject toJSON(AbstractModifier modifier)
    {
        JsonObject object = new JsonObject();

        object.addProperty("type", ModifierRegistry.NAME_TO_CLASS.inverse().get(modifier.getClass()));
        modifier.toJSON(object);

        return object;
    }

    /**
     * Register given modifier 
     */
    public static void register(String name, Class<? extends AbstractModifier> clazz)
    {
        if (CLASS_TO_ID.containsKey(clazz))
        {
            return;
        }

        CLASS_TO_ID.put(clazz, NEXT_ID);
        NAME_TO_CLASS.put(name, clazz);

        NEXT_ID++;
    }

    /**
     * Register client information 
     */
    @SideOnly(Side.CLIENT)
    public static void registerClient(Class<? extends AbstractModifier> clazz, String title, Color color)
    {
        Byte type = CLASS_TO_ID.get(clazz);

        if (type == null)
        {
            return;
        }

        CLIENT.put(clazz, new ModifierInfo(type.byteValue(), title, color));
    }

    /**
     * Modifier information 
     */
    @SideOnly(Side.CLIENT)
    public static class ModifierInfo
    {
        public byte type;
        public String title;
        public Color color;

        public ModifierInfo(byte type, String title, Color color)
        {
            this.type = type;
            this.title = title;
            this.color = color;
        }

        public String getTitle()
        {
            return I18n.format(this.title);
        }
    }
}