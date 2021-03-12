package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.data.StructureBase;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.values.ValueEnvelope;
import mchorse.aperture.camera.values.ValueModifiers;
import mchorse.mclib.config.values.ValueBoolean;

/**
 * Abstract camera modifier
 * 
 * Camera modifiers are special blocks of logic which post-processes 
 * {@link Position} after it was computed by an {@link AbstractFixture}. 
 */
public abstract class AbstractModifier extends StructureBase
{
    public static final Position temporary = new Position();

    /**
     * Whether this modifier is enabled 
     */
    public final ValueBoolean enabled = new ValueBoolean("enabled", true);

    /**
     * Envelope configuration
     */
    public final ValueEnvelope envelope = new ValueEnvelope("envelope");

    /**
     * Apply camera modifiers
     */
    public static void applyModifiers(CameraProfile profile, AbstractFixture fixture, long ticks, long offset, float partialTick, float previewPartialTick, Position pos)
    {
        long duration = fixture == null ? profile.getDuration() : fixture.getDuration();
        ValueModifiers modifiers = fixture == null ? profile.modifiers : fixture.modifiers;

        for (int i = 0; i < modifiers.size(); i++)
        {
            AbstractModifier modifier = modifiers.get(i);

            if (!modifier.enabled.get())
            {
                continue;
            }

            float factor = modifier.envelope.get().factorEnabled(duration, offset + previewPartialTick);

            temporary.copy(pos);
            modifier.modify(ticks, offset, fixture, partialTick, previewPartialTick, profile, temporary);

            if (factor != 0)
            {
                pos.interpolate(temporary, factor);
            }
        }
    }

    public AbstractModifier()
    {
        this.register(this.enabled);
        this.register(this.envelope);
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
}