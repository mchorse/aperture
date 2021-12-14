package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.values.ValuePoint;
import mchorse.mclib.config.values.ValueBoolean;
import net.minecraft.entity.Entity;

/**
 * Look modifier
 * 
 * This modifier locks fixture's angle so it would always look in the 
 * direction of entity. Relative yaw and pitch is also supported.
 */
public class LookModifier extends EntityModifier
{
    public final ValueBoolean relative = new ValueBoolean("relative");
    public final ValueBoolean atBlock = new ValueBoolean("atBlock");
    public final ValueBoolean forward = new ValueBoolean("forward");
    public final ValuePoint block = new ValuePoint("block", new Point(0, 0, 0));

    public LookModifier()
    {
        super();

        this.register(this.relative);
        this.register(this.atBlock);
        this.register(this.forward);
        this.register(this.block);
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        if (this.checkForDead())
        {
            this.tryFindingEntity();
        }

        boolean atBlock = this.atBlock.get();
        boolean forward = this.forward.get();

        if (this.entities == null && !(atBlock || forward))
        {
            return;
        }

        if (fixture != null)
        {
            if (forward)
            {
                fixture.applyFixture(offset - 1, partialTick, previewPartialTick, profile, this.position);
            }
            else
            {
                fixture.applyFixture(0, 0, 0, profile, this.position);
            }
        }
        else
        {
            this.position.copy(pos);
        }

        double x = 0;
        double y = 0;
        double z = 0;

        if (atBlock)
        {
            Point block = this.block.get();

            x = block.x;
            y = block.y;
            z = block.z;
        }
        else if (!forward)
        {
            double sx = 0;
            double sy = 0;
            double sz = 0;
            int size = this.entities.size();

            for (Entity entity : this.entities)
            {
                sx += entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTick;
                sy += entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTick;
                sz += entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTick;
            }

            x = sx / size;
            y = sy / size;
            z = sz / size;
        }

        Point point = this.offset.get();

        x += point.x;
        y += point.y;
        z += point.z;

        double dX = x - pos.point.x;
        double dY = y - pos.point.y;
        double dZ = z - pos.point.z;

        if (forward)
        {
            dX = pos.point.x - this.position.point.x;
            dY = pos.point.y - this.position.point.y;
            dZ = pos.point.z - this.position.point.z;
        }

        Angle angle = Angle.angle(dX, dY, dZ);

        float yaw = angle.yaw;
        float pitch = angle.pitch;

        if (this.relative.get() && !forward)
        {
            yaw += pos.angle.yaw - this.position.angle.yaw;
            pitch += pos.angle.pitch - this.position.angle.pitch;
        }

        pos.angle.set(yaw, pitch);
    }

    @Override
    public AbstractModifier create()
    {
        return new LookModifier();
    }
}