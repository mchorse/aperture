package mchorse.aperture.camera.fixtures;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.values.ValuePosition;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Idle camera fixture
 *
 * This fixture is the basic fixture type. This fixture is responsible for
 * outputting static values for camera.
 */
public class IdleFixture extends AbstractFixture
{
    public final ValuePosition position = new ValuePosition("position");

    public IdleFixture(long duration)
    {
        super(duration);

        this.register(this.position);
    }

    @Override
    public void fromPlayer(EntityPlayer player)
    {
        this.position.get().set(player);
    }

    @Override
    public void applyFixture(long ticks, float partialTicks, float previewPartialTick, CameraProfile profile, Position pos)
    {
        pos.copy(this.position.get());
    }

    @Override
    public AbstractFixture create(long duration)
    {
        return new IdleFixture(duration);
    }

    @Override
    public void copy(AbstractFixture from)
    {
        super.copy(from);

        if (from instanceof IdleFixture)
        {
            this.position.copy(((IdleFixture) from).position);
        }
    }

    /* Save/load methods */

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.position.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.position.toBytes(buffer);
    }
}