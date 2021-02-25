package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.values.ValuePoint;

/**
 * Translate camera modifier
 * 
 * This camera modifier is basically translates the position of 
 * calculated camera fixture by stored X, Y and Z.
 */
public class TranslateModifier extends AbstractModifier
{
    public final ValuePoint translate = new ValuePoint("translate", new Point(0, 0, 0));

    public TranslateModifier()
    {
        super();

        this.register(this.translate);
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        Point point = this.translate.get();

        pos.point.x += point.x;
        pos.point.y += point.y;
        pos.point.z += point.z;
    }

    @Override
    public AbstractModifier create()
    {
        return new TranslateModifier();
    }
}