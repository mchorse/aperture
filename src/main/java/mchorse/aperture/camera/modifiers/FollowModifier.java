package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

/**
 * Follow modifier
 * 
 * This modifier is responsible for binding camera to given entity. For 
 * moving fixtures such as paths, follow fixture calculates relative 
 * so the path movement is still possible.
 */
public class FollowModifier extends EntityModifier
{
    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, Position pos)
    {
        if (this.entity == null || this.entity.isDead)
        {
            this.tryFindingEntity();
        }

        if (this.entity == null)
        {
            return;
        }

        fixture.applyFixture(0, 0, this.position);

        float x = (float) (this.entity.lastTickPosX + (this.entity.posX - this.entity.lastTickPosX) * partialTick);
        float y = (float) (this.entity.lastTickPosY + (this.entity.posY - this.entity.lastTickPosY) * partialTick);
        float z = (float) (this.entity.lastTickPosZ + (this.entity.posZ - this.entity.lastTickPosZ) * partialTick);

        x += pos.point.x - this.position.point.x;
        y += pos.point.y - this.position.point.y;
        z += pos.point.z - this.position.point.z;

        pos.point.set(x, y, z);
    }

    @Override
    public AbstractModifier clone()
    {
        FollowModifier modifier = new FollowModifier();

        modifier.enabled = this.enabled;
        modifier.selector = this.selector;

        return modifier;
    }
}