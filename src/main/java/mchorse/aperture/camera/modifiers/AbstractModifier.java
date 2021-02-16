package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.smooth.Envelope;
import mchorse.mclib.network.IByteBufSerializable;

import java.util.List;

/**
 * Abstract camera modifier
 * 
 * Camera modifiers are special blocks of logic which post-processes 
 * {@link Position} after it was computed by an {@link AbstractFixture}. 
 */
public abstract class AbstractModifier implements IByteBufSerializable
{
    public static final Position temporary = new Position();

    /**
     * Whether this modifier is enabled 
     */
    @Expose
    public boolean enabled = true;

    /**
     * Envelope configuration
     */
    @Expose
    public Envelope envelope = new Envelope();

    /**
     * Apply camera modifiers
     */
    public static void applyModifiers(CameraProfile profile, AbstractFixture fixture, long ticks, long offset, float partialTick, float previewPartialTick, Position pos)
    {
        long duration = fixture == null ? profile.getDuration() : fixture.getDuration();
        List<AbstractModifier> modifiers = fixture == null ? profile.getModifiers() : fixture.getModifiers();

        for (AbstractModifier modifier : modifiers)
        {
            if (!modifier.enabled)
            {
                continue;
            }

            float factor = modifier.envelope.factorEnabled(duration, offset + previewPartialTick);

            temporary.copy(pos);
            modifier.modify(ticks, offset, fixture, partialTick, previewPartialTick, profile, temporary);

            if (factor != 0)
            {
                pos.interpolate(temporary, factor);
            }
        }
    }

    /**
     * Modify (apply, filter, process, however you name it) modifier on given position
     *
     * @param ticks - Amount of ticks from start
     * @param offset - Amount of ticks from current camera fixture
     * @param fixture - Currently running camera fixture
     */
    public abstract void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos);

    public final AbstractModifier copy()
    {
        AbstractModifier modifier = this.create();

        modifier.copy(this);

        return modifier;
    }

    public abstract AbstractModifier create();

    public void copy(AbstractModifier from)
    {
        this.enabled = from.enabled;
        this.envelope.copy(from.envelope);
    }

    public void toJSON(JsonObject object)
    {
        JsonElement element = object.get("envelope");

        if (this.envelope != null && element != null && element.isJsonObject())
        {
            this.envelope.toJSON(element.getAsJsonObject());
        }
    }

    public void fromJSON(JsonObject object)
    {
        if (this.envelope == null)
        {
            this.envelope = new Envelope();

            JsonElement element = object.get("envelope");

            if (element != null && element.isJsonObject())
            {
                this.envelope.fromJSON(element.getAsJsonObject());
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeBoolean(this.enabled);
        this.envelope.fromBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.enabled = buffer.readBoolean();
        this.envelope.toBytes(buffer);
    }
}