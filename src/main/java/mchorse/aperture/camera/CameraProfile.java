package mchorse.aperture.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.events.CameraProfileChangedEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

/**
 * Camera profile class
 *
 * This class represents the camera profile. Camera profile is a list of
 * camera fixtures that can be used to playback the camera movement and can be
 * loaded/saved to the disk.
 */
public class CameraProfile
{
    /**
     * Pattern for finding numbered
     */
    public static final Pattern NUMBERED_SUFFIX = Pattern.compile("_(\\d+)$");

    /**
     * List of profile's camera fixtures
     */
    @Expose
    protected List<AbstractFixture> fixtures = new ArrayList<AbstractFixture>();

    /**
     * Where the camera profile is stored. Needs only on the client 
     */
    protected AbstractDestination destination;

    /**
     * Whether camera profile was modified
     */
    public boolean dirty;

    public CameraProfile(AbstractDestination destination)
    {
        this.destination = destination;
    }

    public AbstractDestination getDestination()
    {
        return this.destination;
    }

    public void setDestination(AbstractDestination destination)
    {
        this.destination = destination;
        this.dirty();
    }

    /**
     * Get duration of current camera profile
     */
    public long getDuration()
    {
        long duration = 0;

        for (AbstractFixture fixture : this.fixtures)
        {
            duration += fixture.getDuration();
        }

        return duration;
    }

    public void dirty()
    {
        this.setDirty(true);
    }

    /**
     * Set camera profile dirty and also post an event 
     */
    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;

        MinecraftForge.EVENT_BUS.post(new CameraProfileChangedEvent(this));
    }

    /**
     * Calculate offset (in ticks) of the given target (not target based)
     * fixture. Input target fixture must be out of current camera profile.
     */
    public long calculateOffset(AbstractFixture target)
    {
        long tick = 0;

        for (AbstractFixture fixture : this.fixtures)
        {
            if (fixture == target)
            {
                break;
            }

            tick += fixture.getDuration();
        }

        return tick;
    }

    /**
     * Calculate offset of a fixture located at given tick optionally
     */
    public long calculateOffset(long tick, boolean next)
    {
        long sum = 0;

        for (AbstractFixture fixture : this.fixtures)
        {
            long duration = fixture.getDuration();
            sum += duration;

            if (sum > tick)
            {
                if (!next)
                {
                    sum -= duration;
                }

                break;
            }
        }

        return sum;
    }

    /**
     * Get fixture which runs during given tick (approximately)
     */
    public AbstractFixture atTick(long tick)
    {
        if (tick >= this.getDuration() || tick < 0)
        {
            return null;
        }

        long pos = 0;
        AbstractFixture out = null;

        for (AbstractFixture fixture : this.fixtures)
        {
            if (tick < pos)
            {
                break;
            }

            pos += fixture.getDuration();
            out = fixture;
        }

        return out;
    }

    /**
     * Get the amount of fixtures in current profile
     */
    public int getCount()
    {
        return this.fixtures.size();
    }

    /**
     * Get fixture at specified index
     */
    public AbstractFixture get(int index)
    {
        return this.has(index) ? this.fixtures.get(index) : null;
    }

    /**
     * Checks if fixture at specified index is exists
     */
    public boolean has(int index)
    {
        return index >= 0 && index < this.fixtures.size();
    }

    /**
     * Get all of the fixtures
     */
    public List<AbstractFixture> getAll()
    {
        return this.fixtures;
    }

    /**
     * Add a fixture in the camera profile
     */
    public void add(AbstractFixture fixture)
    {
        this.fixtures.add(fixture);
        this.dirty();
    }

    /**
     * Add a fixture in the camera profile after given index
     */
    public void add(AbstractFixture fixture, int index)
    {
        if (index + 1 < this.fixtures.size())
        {
            this.fixtures.add(index, fixture);
        }
        else
        {
            this.fixtures.add(fixture);
        }

        this.dirty();
    }

    /**
     * Move fixture on index {@code from} to index {@code to}
     */
    public void move(int from, int to)
    {
        if (this.has(from) && this.has(to))
        {
            this.fixtures.add(to, this.fixtures.remove(from));
            this.dirty();
        }
    }

    /**
     * Remove fixture at specified index
     */
    public void remove(int index)
    {
        int size = this.fixtures.size();

        if (size != 0 && index >= 0 && index < size)
        {
            this.fixtures.remove(index);
            this.dirty();
        }
    }

    /**
     * Reset camera profile (remove all fixtures in profile)
     */
    public void reset()
    {
        this.fixtures.clear();
        this.dirty();
    }

    /**
     * Apply camera profile transformation at given time on passed position
     */
    public void applyProfile(long progress, float partialTick, Position position)
    {
        int index = 0;
        long originalProgress = progress;

        for (AbstractFixture fixture : this.fixtures)
        {
            long duration = fixture.getDuration();

            if (progress < duration) break;

            progress -= duration;
            index += 1;
        }

        if (index >= this.fixtures.size())
        {
            return;
        }

        AbstractFixture fixture = this.fixtures.get(index);

        if (progress == 0)
        {
            fixture.preApplyFixture(progress, position);
        }

        fixture.applyFixture(progress, partialTick, position);
        fixture.applyModifiers(originalProgress, progress, partialTick, position);
    }

    /**
     * Read camera profile from a byte buffer 
     */
    public void fromByteBuf(ByteBuf buffer)
    {
        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractFixture fixture = FixtureRegistry.fromByteBuf(buffer);

            if (fixture != null)
            {
                this.fixtures.add(fixture);
            }
        }
    }

    /**
     * Write camera profile to a byte buffer 
     */
    public void toByteBuf(ByteBuf buffer)
    {
        buffer.writeInt(this.fixtures.size());

        for (AbstractFixture fixture : this.fixtures)
        {
            FixtureRegistry.toByteBuf(fixture, buffer);
        }
    }

    /**
     * Save camera profile based on destination 
     */
    public void save()
    {
        if (this.destination != null)
        {
            this.destination.save(this);
        }

        this.setDirty(false);
    }

    /**
     * Clone this camera profile 
     */
    @Override
    public CameraProfile clone()
    {
        /* Increment filename */
        ResourceLocation path = this.destination.toResourceLocation();
        String filename = path.getResourcePath();
        Matcher matcher = NUMBERED_SUFFIX.matcher(filename);

        if (matcher.find())
        {
            filename = filename.substring(0, matcher.start()) + "_" + (Integer.parseInt(matcher.group(1)) + 1);
        }
        else
        {
            filename = filename + "_1";
        }

        AbstractDestination dest = AbstractDestination.fromResourceLocation(new ResourceLocation(path.getResourceDomain(), filename));
        CameraProfile profile = new CameraProfile(dest);

        /* Copy fixtures */
        for (AbstractFixture fixture : this.fixtures)
        {
            profile.fixtures.add(fixture.clone());
        }

        return profile;
    }
}