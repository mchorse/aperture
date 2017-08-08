package mchorse.aperture.camera.fixtures;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.Position;
import mchorse.aperture.commands.SubCommandBase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Abstract camera fixture
 *
 * Camera fixtures are the special types of class that store camera
 * transformations based on some variables.
 *
 * Every fixture have duration field.
 */
public abstract class AbstractFixture
{
    /* Types of camera fixtures */
    public static final byte IDLE = 1;
    public static final byte PATH = 2;
    public static final byte LOOK = 3;
    public static final byte FOLLOW = 4;
    public static final byte CIRCULAR = 5;

    /**
     * A mapping between string named to byte type of the fixture
     */
    public static final Map<String, Byte> STRING_TO_TYPE = new HashMap<String, Byte>();

    static
    {
        STRING_TO_TYPE.put("idle", IDLE);
        STRING_TO_TYPE.put("path", PATH);
        STRING_TO_TYPE.put("look", LOOK);
        STRING_TO_TYPE.put("follow", FOLLOW);
        STRING_TO_TYPE.put("circular", CIRCULAR);
    }

    /**
     * Duration of this fixture. Represented in ticks. There are 20 ticks in a
     * second.
     */
    @Expose
    protected long duration;

    /**
     * The name of this camera fixture. Added just for organization.
     */
    @Expose
    protected String name = "";

    /**
     * This is abstract's fixture factory method.
     *
     * It's responsible creating a camera fixture from type.
     */
    public static AbstractFixture fromType(byte type, long duration) throws Exception
    {
        if (type == IDLE) return new IdleFixture(duration);
        else if (type == PATH) return new PathFixture(duration);
        else if (type == LOOK) return new LookFixture(duration);
        else if (type == FOLLOW) return new FollowFixture(duration);
        else if (type == CIRCULAR) return new CircularFixture(duration);

        throw new Exception("Camera fixture by type '" + type + "' wasn't found!");
    }

    /**
     * Create an abstract camera fixture out of byte buffer
     */
    public static AbstractFixture readFromByteBuf(ByteBuf buffer)
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
            System.out.println("Error reading abstract camera fixture from ByteBuf of type " + type + " and " + duration + "t duration!!!");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This is another abstract's fixture factory method.
     *
     * It's responsible for creating a fixture from command line arguments and
     * player's space attributes (i.e. position and rotation).
     *
     * Commands can also be updated using {@link #edit(String[], EntityPlayer)}
     * method.
     */
    public static AbstractFixture fromCommand(String[] args, EntityPlayer player) throws CommandException
    {
        if (args.length < 2 || player == null)
        {
            throw new CommandException("fixture.few_args");
        }

        String type = args[0];
        long duration = CommandBase.parseLong(args[1], 1, Long.MAX_VALUE);
        AbstractFixture fixture;

        try
        {
            fixture = fromType(STRING_TO_TYPE.get(type), duration);
        }
        catch (Exception e)
        {
            throw new CommandException("fixture.wrong_type", type);
        }

        fixture.edit(SubCommandBase.dropFirstArguments(args, 2), player);

        return fixture;
    }

    public AbstractFixture(long duration)
    {
        this.setDuration(duration);
    }

    /* Duration management */

    /**
     * Set duration (duration is in milliseconds)
     */
    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    /**
     * Get duration
     */
    public long getDuration()
    {
        return this.duration;
    }

    /* Name management */

    /**
     * Set name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get name
     */
    public String getName()
    {
        return this.name == null ? "" : this.name;
    }

    /* JSON (de)serialization methods */

    public void fromJSON(JsonObject object)
    {}

    public void toJSON(JsonObject object)
    {}

    /* ByteBuf (de)serialization methods */

    /**
     * Read abstract fixture's properties from byte buffer 
     */
    public void fromByteBuf(ByteBuf buffer)
    {
        this.name = ByteBufUtils.readUTF8String(buffer);
    }

    /**
     * Write this abstract fixture to the byte buffer 
     */
    public void toByteBuf(ByteBuf buffer)
    {
        buffer.writeByte(this.getType());
        buffer.writeLong(this.duration);
        ByteBufUtils.writeUTF8String(buffer, this.name);
    }

    /* Abstract methods */

    /**
     * Edit this fixture with given CLI arguments and given player. For every
     * fixture the editing process may vary.
     */
    public abstract void edit(String args[], EntityPlayer player) throws CommandException;

    /**
     * Apply this fixture onto position
     */
    public abstract void applyFixture(long ticks, float partialTick, Position pos);

    /**
     * Before applying this fixture
     */
    public void preApplyFixture(long ticks, Position pos)
    {}

    /**
     * Pre apply and apply this fixture
     */
    public void preAndApplyFixture(long ticks, float partialTick, Position pos)
    {
        this.preApplyFixture(ticks, pos);
        this.applyFixture(ticks, partialTick, pos);
    }

    /**
     * Get the type of this fixture
     */
    public abstract byte getType();
}