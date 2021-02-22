package mchorse.aperture.camera.fixtures;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.mclib.config.ConfigCategory;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.ValueInt;
import mchorse.mclib.config.values.ValueLong;
import mchorse.mclib.config.values.ValueString;
import mchorse.mclib.network.IByteBufSerializable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract camera fixture
 *
 * Camera fixtures are the special types of class that store camera
 * transformations based on some variables.
 *
 * Every fixture have duration field.
 */
public abstract class AbstractFixture implements IByteBufSerializable
{
    protected ConfigCategory category = new ConfigCategory("");

    /**
     * The name of this camera fixture. Added just for organization.
     */
    public final ValueString name = new ValueString("name", "");

    /**
     * Custom color tint for fixtures
     */
    public final ValueInt color = new ValueInt("color", 0x000000);

    /**
     * Duration of this fixture. Represented in ticks. There are 20 ticks in a
     * second.
     */
    public final ValueLong duration = new ValueLong("duration", 1);

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

        this.register(this.name);
        this.register(this.color);
        this.register(this.duration);
    }

    protected void register(IConfigValue value)
    {
        this.category.values.put(value.getId(), value);
    }

    public IConfigValue getProperty(String name)
    {
        IConfigValue value = this.category.values.get(name);

        if (value == null && name.contains("."))
        {
            String[] splits = name.split("\\.");

            value = this.searchRecursively(this.category.values.get(splits[0]), splits, 0, splits[0], name);
        }

        return value;
    }

    private IConfigValue searchRecursively(IConfigValue value, String[] splits, int i, String prefix, String name)
    {
        if (value == null)
        {
            return null;
        }

        for (IConfigValue child : value.getSubValues())
        {
            if (child.getId().equals(name))
            {
                return child;
            }
            else if (i + 1 < splits.length && child.getId().equals(prefix))
            {
                return this.searchRecursively(child, splits, i + 1, child.getId(), name);
            }
        }

        return null;
    }

    public void initiate()
    {}

    /* Color */

    public void setColor(int color)
    {
        this.color.set(color);
    }

    public int getColor()
    {
        return this.color.get();
    }

    /* Duration management */

    /**
     * Set duration (duration is in milliseconds)
     */
    public void setDuration(long duration)
    {
        this.duration.set(duration);
    }

    /**
     * Get duration
     */
    public long getDuration()
    {
        return this.duration.get();
    }

    /* Name management */

    /**
     * Set name
     */
    public void setName(String name)
    {
        this.name.set(name);
    }

    /**
     * Get name
     */
    public String getName()
    {
        return this.name.get();
    }

    /**
     * Get some properties from player upon creation
     */
    public void fromPlayer(EntityPlayer player)
    {}

    /* JSON (de)serialization methods */

    public void fromJSON(JsonObject object)
    {
        for (IConfigValue value : this.category.values.values())
        {
            if (object.has(value.getId()))
            {
                value.fromJSON(object.get(value.getId()));
            }
        }

        if (this.getDuration() <= 0)
        {
            this.setDuration(1);
        }
    }

    public void toJSON(JsonObject object)
    {
        for (IConfigValue value : this.category.values.values())
        {
            object.add(value.getId(), value.toJSON());
        }
    }

    /* ByteBuf (de)serialization methods */

    /**
     * Read abstract fixture's properties from byte buffer 
     */
    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.name.set(ByteBufUtils.readUTF8String(buffer));
        this.color.set(buffer.readInt());

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractModifier modifier = ModifierRegistry.fromBytes(buffer);

            if (modifier != null)
            {
                this.modifiers.add(modifier);
            }
        }
    }

    /**
     * Write this abstract fixture to the byte buffer 
     */
    @Override
    public void toBytes(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, this.name.get());
        buffer.writeInt(this.color.get());

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
                ModifierRegistry.toBytes(modifier, buffer);
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

        this.name.copy(from.name);
        this.color.copy(from.color);
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