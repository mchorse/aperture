package mchorse.aperture.camera;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.events.CameraProfileChangedEvent;
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
        return this.fixtures.get(index);
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
     * Move fixture on index {@code from} to index {@code to}
     */
    public void move(int from, int to)
    {
        if (this.has(from) && this.has(to))
        {
            this.fixtures.add(to, this.fixtures.remove(from));
        }

        this.dirty();
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
        fixture.applyModifiers(originalProgress, partialTick, position);
    }

    /**
     * Read camera profile from a byte buffer 
     */
    public void fromByteBuf(ByteBuf buffer)
    {
        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractFixture fixture = AbstractFixture.readFromByteBuf(buffer);

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
            fixture.toByteBuf(buffer);
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
}