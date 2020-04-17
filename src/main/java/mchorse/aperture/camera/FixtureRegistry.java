package mchorse.aperture.camera;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.utils.Color;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

/**
 * Fixture registry
 * 
 * That's the place where you register all camera fixture
 */
public class FixtureRegistry
{
    /**
     * Bi-directional map between class and byte ID
     */
    public static final BiMap<Class<? extends AbstractFixture>, Byte> CLASS_TO_ID = HashBiMap.create();

    /**
     * Bi-directional map  map of fixtures types mapped to corresponding 
     * class
     */
    public static final BiMap<String, Class<? extends AbstractFixture>> NAME_TO_CLASS = HashBiMap.create();

    /**
     * A mapping between string named to byte type of the fixture
     */
    public static final Map<String, Byte> NAME_TO_ID = new HashMap<String, Byte>();

    /**
     * Client information about camera fixtures, such as title, color, etc.
     */
    @SideOnly(Side.CLIENT)
    public static Map<Class<? extends AbstractFixture>, FixtureInfo> CLIENT;

    /**
     * Next available id 
     */
    private static byte NEXT_ID = 0;

    /**
     * Create camera from type
     */
    public static AbstractFixture fromType(byte type, long duration) throws Exception
    {
        Class<? extends AbstractFixture> clazz = CLASS_TO_ID.inverse().get(type);

        if (clazz == null)
        {
            throw new Exception("Camera fixture by type '" + type + "' wasn't found!");
        }

        return clazz.getConstructor(long.class).newInstance(duration);
    }

    /**
     * Write a camera fixture to byte buffer 
     */
    public static void toByteBuf(AbstractFixture fixture, ByteBuf buffer)
    {
        byte type = CLASS_TO_ID.get(fixture.getClass());

        buffer.writeByte(type);
        buffer.writeLong(fixture.getDuration());

        fixture.toByteBuf(buffer);
    }

    /**
     * Create an abstract camera fixture out of byte buffer
     */
    public static AbstractFixture fromByteBuf(ByteBuf buffer)
    {
        byte type = buffer.readByte();
        long duration = buffer.readLong();

        try
        {
            AbstractFixture fixture = fromType(type, duration);

            fixture.fromByteBuf(buffer);

            return fixture;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Register given camera fixture
     */
    public static void register(String name, Class<? extends AbstractFixture> clazz)
    {
        if (CLASS_TO_ID.containsKey(clazz))
        {
            return;
        }

        CLASS_TO_ID.put(clazz, NEXT_ID);
        NAME_TO_ID.put(name, NEXT_ID);
        NAME_TO_CLASS.put(name, clazz);

        NEXT_ID++;
    }

    /**
     * Register client fixture information 
     */
    @SideOnly(Side.CLIENT)
    public static void registerClient(Class<? extends AbstractFixture> clazz, String title, Color color)
    {
        Byte type = CLASS_TO_ID.get(clazz);

        if (type == null)
        {
            return;
        }

        CLIENT.put(clazz, new FixtureInfo(type.byteValue(), title, color));
    }

    /**
     * Fixture information
     */
    @SideOnly(Side.CLIENT)
    public static class FixtureInfo
    {
        public byte type;
        public String title;
        public Color color;

        public FixtureInfo(byte type, String title, Color color)
        {
            this.type = type;
            this.title = title;
            this.color = color;
        }
    }
}