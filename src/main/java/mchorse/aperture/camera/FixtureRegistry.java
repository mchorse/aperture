package mchorse.aperture.camera;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.json.AbstractFixtureAdapter;
import mchorse.aperture.utils.Color;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Fixture registry
 * 
 * That's the place where you register all camera fixture
 */
public class FixtureRegistry
{
    /**
     * Bi-directional map between 
     */
    public static final BiMap<Class<? extends AbstractFixture>, Byte> MAP = HashBiMap.create();

    /**
     * A mapping between string named to byte type of the fixture
     */
    public static final Map<String, Byte> STRING_TO_TYPE = new HashMap<String, Byte>();

    /**
     * Client information about camera fixtures, such as title, color, etc.
     */
    @SideOnly(Side.CLIENT)
    public static final Map<Class<? extends AbstractFixture>, FixtureInfo> CLIENT = new HashMap<Class<? extends AbstractFixture>, FixtureInfo>();

    /**
     * Next available id 
     */
    private static byte NEXT_ID = 0;

    /**
     * Create camera from type
     */
    public static AbstractFixture fromType(byte type, long duration) throws Exception
    {
        Class<? extends AbstractFixture> clazz = MAP.inverse().get(type);

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
        byte type = MAP.get(fixture.getClass());

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
        MAP.put(clazz, NEXT_ID);
        STRING_TO_TYPE.put(name, NEXT_ID);
        AbstractFixtureAdapter.TYPES.put(name, clazz);

        NEXT_ID++;
    }

    /**
     * Register client fixture information 
     */
    @SideOnly(Side.CLIENT)
    public static void registerClient(Class<? extends AbstractFixture> clazz, String title, Color color)
    {
        Byte type = MAP.get(clazz);

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