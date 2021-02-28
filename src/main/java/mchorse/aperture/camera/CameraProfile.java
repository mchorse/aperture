package mchorse.aperture.camera;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.data.StructureBase;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.values.ValueCurves;
import mchorse.aperture.camera.values.ValueFixtures;
import mchorse.aperture.camera.values.ValueModifiers;
import mchorse.aperture.events.CameraProfileChangedEvent;
import mchorse.aperture.utils.undo.UndoManager;
import mchorse.mclib.network.IByteBufSerializable;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * List of profile's camera fixtures
     */
    public final ValueFixtures fixtures = new ValueFixtures("fixtures");

    /**
     * List of profile's global camera fixtures
     */
    public final ValueModifiers modifiers = new ValueModifiers("modifiers");

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
        this.register(this.fixtures);
        this.register(this.modifiers);
    }

    public Map<String, KeyframeChannel> getCurves()
    {
        return this.curves.get();
    }

    public List<AbstractFixture> getFixtures()
    {
        return this.fixtures.get();
    }

    public List<AbstractModifier> getModifiers()
    {
        return this.modifiers.get();
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

        for (AbstractFixture fixture : this.getFixtures())
        {
            duration += fixture.getDuration();
        }

        return duration;
    }

    public void initiate()
    {
        for (AbstractFixture fixture : this.getFixtures())
        {
            fixture.initiate();
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

        for (AbstractFixture fixture : this.getFixtures())
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

        for (AbstractFixture fixture : this.getFixtures())
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

        for (AbstractFixture fixture : this.getFixtures())
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
        return this.getFixtures().size();
    }

    /**
     * Get fixture at specified index
     */
    public AbstractFixture get(int index)
    {
        return this.has(index) ? this.getFixtures().get(index) : null;
    }

    /**
     * Checks if fixture at specified index is exists
     */
    public boolean has(int index)
    {
        return index >= 0 && index < this.getCount();
    }

    /**
     * Add a fixture in the camera profile
     */
    public void add(AbstractFixture fixture)
    {
        this.getFixtures().add(fixture);
        this.dirty();
    }

    /**
     * Add a fixture in the camera profile after given index
     */
    public void add(AbstractFixture fixture, int index)
    {
        if (index < this.getCount())
        {
            this.getFixtures().add(index, fixture);
        }
        else
        {
            this.getFixtures().add(fixture);
        }

        this.dirty();
    }

    /**
     * Remove fixture at specified index
     */
    public void remove(int index)
    {
        int size = this.getCount();

        if (index >= 0 && index < size)
        {
            this.getFixtures().remove(index);
            this.dirty();
        }
    }

    /**
     * Apply different curves
     */
    @SideOnly(Side.CLIENT)
    public void applyCurves(long progress, float partialTick)
    {
        KeyframeChannel channel = this.curves.get().get("brightness");

        if (channel != null && !channel.isEmpty())
        {
            Minecraft.getMinecraft().gameSettings.gammaSetting = (float) channel.interpolate(progress + partialTick);
        }
    }

    public void applyProfile(long progress, float partialTick, Position position)
    {
        this.applyProfile(progress, partialTick, partialTick, position);
    }

    public void applyProfile(long progress, float partialTick, float previewPartialTick, Position position)
    {
        this.applyProfile(progress, partialTick, previewPartialTick, position, true);
    }

    public void applyProfile(long progress, float partialTick, Position position, boolean modifiers)
    {
        this.applyProfile(progress, partialTick, partialTick, position, modifiers);
    }

    /**
     * Apply camera profile transformation at given time on passed position
     */
    public void applyProfile(long progress, float partialTick, float previewPartialTick, Position position, boolean modifiers)
    {
        int index = 0;
        long originalProgress = progress;

        for (AbstractFixture fixture : this.getFixtures())
        {
            long duration = fixture.getDuration();

            if (progress < duration) break;

            progress -= duration;
            index += 1;
        }

        if (index >= this.getCount())
        {
            return;
        }

        AbstractFixture fixture = this.getFixtures().get(index);

        if (progress == 0)
        {
            fixture.preApplyFixture(progress, position);
        }

        fixture.applyFixture(progress, partialTick, previewPartialTick, this, position);

        if (modifiers)
        {
            AbstractModifier.applyModifiers(this, fixture, originalProgress, progress, partialTick, previewPartialTick, position);
            AbstractModifier.applyModifiers(this, null, originalProgress, originalProgress, partialTick, previewPartialTick, position);
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
    }
}