package mchorse.aperture.camera.fixtures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.modifiers.AbstractModifier;
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
     * Custom color tint for fixtures
     */
    @Expose
    protected int color = 0x000000;

    /**
     * List of camera modifiers. 
     */
    @Expose
    protected List<AbstractModifier> modifiers = new ArrayList<AbstractModifier>();

    /**
     * Copy given fixture's modifiers to another fixture 
     */
    public static void copyModifiers(AbstractFixture from, AbstractFixture to)
    {
        for (AbstractModifier modifier : from.modifiers)
        {
            AbstractModifier copy = modifier.copy();

            if (copy != null)
            {
                to.modifiers.add(copy);
            }
        }
    }

    /**
     * Default constructor. All subclasses must implement the same 
     * constructor, because {@link FixtureRegistry} depends on it. 
     */
    public AbstractFixture(long duration)
    {
        this.setDuration(duration);
    }

    public void initiate()
    {}

    /* Color */

    public void setColor(int color)
    {
        this.color = color;
    }

    public int getColor()
    {
        return this.color;
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
    {
        if (this.getDuration() <= 0)
        {
            this.setDuration(1);
        }
    }

    public void toJSON(JsonObject object)
    {}

    /* ByteBuf (de)serialization methods */

    /**
     * Read abstract fixture's properties from byte buffer 
     */
    public void fromByteBuf(ByteBuf buffer)
    {
        this.name = ByteBufUtils.readUTF8String(buffer);
        this.color = buffer.readInt();

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
        buffer.writeInt(this.color);

        if (this.modifiers == null)
        {
            buffer.writeInt(0);
        }
        else
        {
            /* Clear all null modifiers (they can appear due to 
             * incorrect JSON parsing) */
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
     * Apply this fixture onto position with same preview partial tick
     */
    public void applyFixture(long ticks, float partialTick, CameraProfile profile, Position pos)
    {
        this.applyFixture(ticks, partialTick, partialTick, profile, pos);
    }

    /**
     * Apply this fixture onto position
     */
    public abstract void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos);

    /**
     * Apply this fixture onto position using a double between 0 and 1
     */
    public void applyFixture(double range, float partialTick, CameraProfile profile, Position pos)
    {
        long ticks = (long) (range * this.getDuration());

        this.applyFixture(ticks, partialTick, profile, pos);
    }

    /**
     * Apply this fixture onto position using a double between 0 and 1
     */
    public void applyLast(CameraProfile profile, Position pos)
    {
        this.applyFixture(this.getDuration(), 0, profile, pos);
    }

    /**
     * Before applying this fixture
     */
    public void preApplyFixture(long ticks, Position pos)
    {}

    /**
     * Get modifiers 
     */
    public List<AbstractModifier> getModifiers()
    {
        return this.modifiers;
    }

    /**
     * Clone this fixture
     */
    public final AbstractFixture copy()
    {
        AbstractFixture modifier = this.create(this.getDuration());

        modifier.copy(this);

        return modifier;
    }

    /**
     * Create new fixture
     */
    public abstract AbstractFixture create(long duration);

    /**
     * Copy data from another fixture
     */
    public void copy(AbstractFixture from)
    {
        AbstractFixture.copyModifiers(from, this);

        this.name = from.name;
        this.color = from.color;
    }

    /**
     * Copy data from another fixture during replacement
     */
	public void copyByReplacing(AbstractFixture from)
    {
        this.copy(from);
        this.setDuration(from.getDuration());
	}
}