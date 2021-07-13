package mchorse.aperture.camera;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.data.StructureBase;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.values.ValueCurves;
import mchorse.aperture.camera.values.ValueFixtures;
import mchorse.aperture.camera.values.ValueModifiers;
import mchorse.aperture.events.CameraProfileChangedEvent;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import mchorse.mclib.utils.undo.UndoManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Camera profile class
 *
 * This class represents the camera profile. Camera profile is a list of
 * camera fixtures that can be used to playback the camera movement and can be
 * loaded/saved to the disk.
 */
public class CameraProfile extends StructureBase
{
    /**
     * Pattern for finding numbered
     */
    public static final Pattern NUMBERED_SUFFIX = Pattern.compile("_(\\d+)$");

    /**
     * Curves
     */
    public final ValueCurves curves = new ValueCurves("curves");

    /**
     * List of profile's global camera fixtures
     */
    public final ValueModifiers modifiers = new ValueModifiers("modifiers");

    /**
     * List of profile's camera fixtures
     */
    public final ValueFixtures fixtures = new ValueFixtures("fixtures");

    /**
     * Where the camera profile is stored. Needs only on the client 
     */
    protected AbstractDestination destination;

    /**
     * Whether camera profile was modified
     */
    public boolean dirty;

    /**
     * Whether camera profile wasn't saved before
     */
    public boolean exists = true;

    /**
     * Undo manager that tracks changes made to this camera profile
     */
    public UndoManager<CameraProfile> undoManager = new UndoManager<CameraProfile>(30);

    public CameraProfile(AbstractDestination destination)
    {
        super();

        this.destination = destination;

        this.register(this.curves);
        this.register(this.modifiers);
        this.register(this.fixtures);
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

        for (int i = 0; i < this.fixtures.size(); i++)
        {
            duration += this.fixtures.get(i).getDuration();
        }

        return duration;
    }

    public void initiate()
    {
        for (int i = 0; i < this.fixtures.size(); i++)
        {
            this.fixtures.get(i).initiate();
        }
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

        for (int i = 0; i < this.fixtures.size(); i++)
        {
            AbstractFixture fixture = this.fixtures.get(i);

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

        for (int i = 0; i < this.fixtures.size(); i++)
        {
            AbstractFixture fixture = this.fixtures.get(i);

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

        for (int i = 0; i < this.fixtures.size(); i++)
        {
            AbstractFixture fixture = this.fixtures.get(i);

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
    public int size()
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
        return index >= 0 && index < this.size();
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
        if (index < this.size())
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
     * Remove fixture at specified index
     */
    public void remove(int index)
    {
        int size = this.size();

        if (index >= 0 && index < size)
        {
            this.fixtures.remove(index);
            this.dirty();
        }
    }

    /**
     * Apply different curves
     */
    @SideOnly(Side.CLIENT)
    public void applyCurves(long progress, float partialTick)
    {
        ClientProxy.curveManager.applyCurves(this.curves, progress, partialTick);
    }

    public void applyProfile(long progress, float partialTick, Position position)
    {
        this.applyProfile(progress, partialTick, partialTick, position);
    }

    public void applyProfile(long progress, float partialTick, float previewPartialTick, Position position)
    {
        this.applyProfile(progress, partialTick, previewPartialTick, position, true, null);
    }

    public void applyProfile(long progress, float partialTick, Position position, boolean modifiers)
    {
        this.applyProfile(progress, partialTick, partialTick, position, modifiers, null);
    }

    /**
     * Apply camera profile transformation at given time on passed position
     */
    public void applyProfile(long progress, float partialTick, float previewPartialTick, Position position, boolean modifiers, AbstractModifier target)
    {
        int index = 0;
        long originalProgress = progress;

        for (int i = 0; i < this.fixtures.size(); i++)
        {
            AbstractFixture fixture = this.fixtures.get(i);
            long duration = fixture.getDuration();

            if (progress < duration) break;

            progress -= duration;
            index += 1;
        }

        if (index >= this.size())
        {
            if (index == 0)
            {
                return;
            }
            else
            {
                index = this.size() - 1;
                progress = this.fixtures.get(index).getDuration();
            }
        }

        AbstractFixture fixture = this.fixtures.get(index);

        fixture.applyFixture(progress, partialTick, previewPartialTick, this, position);

        if (modifiers && AbstractModifier.applyModifiers(this, fixture, originalProgress, progress, partialTick, previewPartialTick, target, position))
        {
            AbstractModifier.applyModifiers(this, null, originalProgress, originalProgress, partialTick, previewPartialTick, target, position);
        }
    }

    /**
     * Save camera profile based on destination 
     */
    public void save()
    {
        this.exists = true;

        if (this.destination != null)
        {
            this.destination.save(this);
        }

        this.setDirty(false);
    }

    /**
     * Clone this camera profile 
     */
    public CameraProfile copy()
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

        profile.copy(this);
        profile.initiate();

        return profile;
    }

    public void copyFrom(CameraProfile profile)
    {
        this.exists = true;
        this.fixtures.copy(profile.fixtures);
        this.modifiers.copy(profile.modifiers);
        this.curves.copy(profile.curves);
    }
}