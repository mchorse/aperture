package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

/**
 * Abstract camera modifier
 * 
 * Camera modifiers are special blocks of logic which post-processes 
 * {@link Position} after it was computed by an {@link AbstractFixture}. 
 */
public abstract class AbstractModifier
{
    /**
     * Whether this modifier is enabled 
     */
    public boolean enabled = true;

    /**
     * Apply modifier on given position
     */
    public abstract void modify(long ticks, AbstractFixture fixture, float partialTick, Position pos);
}