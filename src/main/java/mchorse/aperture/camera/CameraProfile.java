package mchorse.aperture.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.events.CameraProfileChangedEvent;
import mchorse.mclib.network.IByteBufSerializable;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Camera profile class
 *
 * This class represents the camera profile. Camera profile is a list of
 * camera fixtures that can be used to playback the camera movement and can be
 * loaded/saved to the disk.
 */
public class CameraProfile implements IByteBufSerializable
{
    /**
     * Pattern for finding numbered
     */
    public static final Pattern NUMBERED_SUFFIX = Pattern.compile("_(\\d+)$");

    /**
     * Curves
     */
    @Expose
    protected Map<String, KeyframeChannel> curves = new HashMap<String, KeyframeChannel>();

    /**
     * List of profile's camera fixtures
     */
    @Expose
    protected List<AbstractFixture> fixtures = new ArrayList<AbstractFixture>();

    /**
     * List of profile's global camera fixtures
     */
    @Expose
    protected List<AbstractModifier> modifiers = new ArrayList<AbstractModifier>();

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

    public CameraProfile(AbstractDestination destination)
    {
        this.destination = destination;
    }

    public Map<String, KeyframeChannel> getCurves()
    {
        return this.curves;
    }

    public List<AbstractModifier> getModifiers()
    {
        if (this.modifiers == null)
        {
            this.modifiers = new ArrayList<AbstractModifier>();
        }

        return this.modifiers;
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

    public void initiate()
    {
        for (AbstractFixture fixture : this.fixtures)
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
        if (this.fixtures == null)
        {
            this.fixtures = new ArrayList<AbstractFixture>();
        }

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
            this.fixtures.add(index + 1, fixture);
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
     * Replace a fixture at given frame
     */
    public void replace(AbstractFixture fixture, int index)
    {
        if (this.has(index))
        {
            AbstractFixture present = this.get(index);

            fixture.copyByReplacing(present);
            this.fixtures.set(index, fixture);
        }
    }

    /**
     * Reset camera profile (remove all fixtures in profile)
     */
    public void reset()
    {
        this.getAll().clear();
        this.getModifiers().clear();
        this.dirty();
    }

    /**
     * Apply different curves
     */
    @SideOnly(Side.CLIENT)
    public void applyCurves(long progress, float partialTick)
    {
        KeyframeChannel channel = this.curves.get("brightness");

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

        fixture.applyFixture(progress, partialTick, previewPartialTick, this, position);

        if (modifiers)
        {
            AbstractModifier.applyModifiers(this, fixture, originalProgress, progress, partialTick, previewPartialTick, position);
            AbstractModifier.applyModifiers(this, null, originalProgress, originalProgress, partialTick, previewPartialTick, position);
        }
    }

    /**
     * Read camera profile from a byte buffer 
     */
    @Override
    public void fromBytes(ByteBuf buffer)
    {
        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractFixture fixture = FixtureRegistry.fromBytes(buffer);

            if (fixture != null)
            {
                this.fixtures.add(fixture);
            }
        }

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            AbstractModifier modifier = ModifierRegistry.fromBytes(buffer);

            if (modifier != null)
            {
                this.getModifiers().add(modifier);
            }
        }

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            String key = ByteBufUtils.readUTF8String(buffer);
            KeyframeChannel channel = new KeyframeChannel();

            channel.fromBytes(buffer);
            this.curves.put(key, channel);
        }
    }

    /**
     * Write camera profile to a byte buffer 
     */
    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(this.fixtures.size());

        for (AbstractFixture fixture : this.fixtures)
        {
            FixtureRegistry.toBytes(fixture, buffer);
        }

        buffer.writeInt(this.getModifiers().size());

        for (AbstractModifier modifier : this.getModifiers())
        {
            ModifierRegistry.toBytes(modifier, buffer);
        }

        buffer.writeInt(this.curves.size());

        for (Map.Entry<String, KeyframeChannel> entry : this.curves.entrySet())
        {
            ByteBufUtils.writeUTF8String(buffer, entry.getKey());
            entry.getValue().toBytes(buffer);
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

        /* Copy fixtures */
        for (AbstractFixture fixture : this.getAll())
        {
            profile.fixtures.add(fixture.copy());
        }

        for (AbstractModifier modifier : this.getModifiers())
        {
            profile.modifiers.add(modifier.copy());
        }

        for (Map.Entry<String, KeyframeChannel> entry : this.curves.entrySet())
        {
            KeyframeChannel channel = new KeyframeChannel();

            channel.copy(entry.getValue());
            profile.curves.put(entry.getKey(), channel);
        }

        profile.initiate();

        return profile;
    }

    public void copyFrom(CameraProfile profile)
    {
        this.exists = true;
        this.fixtures = profile.fixtures;
        this.modifiers = profile.modifiers;
    }

    public void cut(int where)
    {
        AbstractFixture fixture = this.atTick(where);

        if (fixture != null)
        {
            long offset = this.calculateOffset(fixture);
            long duration = fixture.getDuration();
            long diff = where - offset;

            if (diff <= 0 || diff >= duration)
            {
                return;
            }

            fixture.setDuration(diff);

            AbstractFixture newFixture = fixture.copy();
            newFixture.setDuration(duration - diff);

            this.add(newFixture, this.fixtures.indexOf(fixture));
        }
    }
}