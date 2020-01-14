package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
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
    public FollowModifier()
    {}

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

        if (fixture != null)
        {
            fixture.applyFixture(0, 0, previewPartialTick, profile, this.position);
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

        pos.point.set(x, y, z);
    }

    @Override
    public AbstractModifier copy()
    {
        FollowModifier modifier = new FollowModifier();

        modifier.enabled = this.enabled;
        modifier.selector = this.selector;

        return modifier;
    }
}