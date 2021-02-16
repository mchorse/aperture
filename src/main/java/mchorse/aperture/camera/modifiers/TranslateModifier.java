package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

/**
 * Translate camera modifier
 * 
 * This camera modifier is basically translates the position of 
 * calculated camera fixture by stored X, Y and Z.
 */
public class TranslateModifier extends AbstractModifier
{
    @Expose
    public Point translate = new Point(0, 0, 0);

    public TranslateModifier()
    {}

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        pos.point.x += this.translate.x;
        pos.point.y += this.translate.y;
        pos.point.z += this.translate.z;
    }

    @Override
    public AbstractModifier create()
    {
        return new TranslateModifier();
    }

    @Override
    public void copy(AbstractModifier from)
    {
        super.copy(from);

        if (from instanceof TranslateModifier)
        {
            this.translate = ((TranslateModifier) from).translate.copy();
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.translate.toBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.translate = Point.fromBytes(buffer);
    }
}