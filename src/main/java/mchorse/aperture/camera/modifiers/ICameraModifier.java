package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

public interface ICameraModifier
{
    /**
     * Modify the position
     */
    public void modify(long ticks, AbstractFixture fixture, float partialTick, Position pos);
}