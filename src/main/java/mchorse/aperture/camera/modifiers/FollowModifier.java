package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

public class FollowModifier extends LookModifier
{
    @Override
    public void modify(long ticks, AbstractFixture fixture, float partialTick, Position pos)
    {
        if (this.entity == null || this.entity.isDead)
        {
            this.tryFindingEntity();
        }

        if (this.entity == null)
        {
            return;
        }

        Position old = new Position(0, 0, 0, 0, 0);

        fixture.applyFixture(0, 0, old);

        float x = (float) (this.entity.lastTickPosX + (this.entity.posX - this.entity.lastTickPosX) * partialTick);
        float y = (float) (this.entity.lastTickPosY + (this.entity.posY - this.entity.lastTickPosY) * partialTick);
        float z = (float) (this.entity.lastTickPosZ + (this.entity.posZ - this.entity.lastTickPosZ) * partialTick);

        pos.point.set(x + (pos.point.x - old.point.x), y + (pos.point.y - old.point.y), z + (pos.point.z - old.point.z));
    }
}