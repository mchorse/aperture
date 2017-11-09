package mchorse.aperture.camera.fixtures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.modifiers.AbstractModifier;
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
     * Camera modifiers 
     */
    @Expose
    protected List<AbstractModifier> modifiers = new ArrayList<AbstractModifier>();

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
            fixture = FixtureRegistry.fromType(FixtureRegistry.STRING_TO_TYPE.get(type), duration);
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

    /**
     * Get some properties from player upon creation  
     */
    public void fromPlayer(EntityPlayer player)
    {}

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

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractModifier modifier = ModifierRegistry.fromByteBuf(buffer);

            if (modifier != null)
            {
                this.modifiers.add(modifier);
            }
        }
    }

    /**
     * Write this abstract fixture to the byte buffer 
     */
    public void toByteBuf(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, this.name);

        if (this.modifiers == null)
        {
            buffer.writeInt(0);
        }
        else
        {
            /* Clear all null modifiers (they can appear due to 
             * incorrect JSON parsing */
            Iterator<AbstractModifier> it = this.modifiers.iterator();

            while (it.hasNext())
            {
                if (it.next() == null)
                {
                    it.remove();
                }
            }

            buffer.writeInt(this.modifiers.size());

            for (AbstractModifier modifier : this.modifiers)
            {
                ModifierRegistry.toByteBuf(modifier, buffer);
            }
        }
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
     * Apply this fixture onto position using a double between 0 and 1
     */
    public void applyFixture(double range, float partialTick, Position pos)
    {
        long ticks = (long) (range * this.getDuration());

        this.applyFixture(ticks, partialTick, pos);
    }

    /**
     * Before applying this fixture
     */
    public void preApplyFixture(long ticks, Position pos)
    {}

    /**
     * Apply camera modifiers
     */
    public void applyModifiers(long ticks, float partialTick, Position pos)
    {
        for (AbstractModifier modifier : this.modifiers)
        {
            if (modifier.enabled)
            {
                modifier.modify(ticks, this, partialTick, pos);
            }
        }
    }

    /**
     * Get modifiers 
     */
    public List<AbstractModifier> getModifiers()
    {
        return this.modifiers;
    }
}