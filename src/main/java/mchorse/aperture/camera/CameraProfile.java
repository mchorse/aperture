package mchorse.aperture.camera;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraProfile;

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
     * Filename of this camera profile (if empty, means new or unsaved)
     */
    protected String filename = "";

    public CameraProfile(String filename)
    {
        this.filename = filename;
    }

    public String getFilename()
    {
        return this.filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
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
        }
    }

    /**
     * Reset camera profile (remove all fixtures in profile)
     */
    public void reset()
    {
        this.fixtures.clear();
    }

    /**
     * Apply camera profile transformation at given time on passed position
     */
    public void applyProfile(long progress, float partialTicks, Position position)
    {
        int index = 0;

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

        fixture.applyFixture((float) progress / fixture.getDuration(), partialTicks, position);
    }

    /**
     * Save camera profile on the server
     */
    public void save()
    {
        if (this.fixtures.size() == 0)
        {
            return;
        }

        Dispatcher.sendToServer(new PacketCameraProfile(this.filename, CameraUtils.toJSON(this)));
    }
}