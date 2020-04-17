package mchorse.aperture.camera;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.mclib.utils.Color;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
     * Write an abstract modifier to byte buffer 
     */
    public static void toByteBuf(AbstractModifier modifier, ByteBuf buffer)
    {
        buffer.writeByte(getType(modifier));

        modifier.toByteBuf(buffer);
    }

    /**
     * Read an abstract modifier from a byte buffer
     */
    public static AbstractModifier fromByteBuf(ByteBuf buffer)
    {
        try
        {
            AbstractModifier modifier = fromType(buffer.readByte());

            modifier.fromByteBuf(buffer);

            return modifier;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
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
    }
}