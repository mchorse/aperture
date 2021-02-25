package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.config.values.ValueBoolean;
import net.minecraft.entity.Entity;

/**
 * Follow modifier
 * 
 * This modifier is responsible for binding camera to given entity. For 
 * moving fixtures such as paths, follow fixture calculates relative 
 * so the path movement is still possible.
 */
public class FollowModifier extends EntityModifier
{
    public final ValueBoolean relative = new ValueBoolean("relative");

    public FollowModifier()
    {
        super();

        this.register(this.relative);
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        if (this.checkForDead())
        {
            this.tryFindingEntity();
        }

        if (this.entities == null)
        {
            return;
        }

        if (fixture != null && this.relative.get())
        {
            fixture.applyFixture(0, 0, 0, profile, this.position);
        }
        else
        {
            this.position.copy(pos);
        }

        double x = 0;
        double y = 0;
        double z = 0;
        int size = this.entities.size();

        for (Entity entity : this.entities)
        {
            x += entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTick;
            y += entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTick;
            z += entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTick;
        }

        x = x / size + pos.point.x - this.position.point.x;
        y = y / size + pos.point.y - this.position.point.y;
        z = z / size + pos.point.z - this.position.point.z;

        Point point = this.offset.get();

        pos.point.set(x + point.x, y + point.y, z + point.z);
    }

    @Override
    public AbstractModifier create()
    {
        return new FollowModifier();
    }
}