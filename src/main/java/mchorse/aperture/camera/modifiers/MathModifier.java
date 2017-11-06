package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

public class MathModifier extends AbstractModifier
{
    public MathModifier()
    {}

    @Override
    public void modify(long ticks, AbstractFixture fixture, float partialTick, Position pos)
    {

    }

    @Override
    public byte getType()
    {
        return AbstractModifier.MATH;
    }
}