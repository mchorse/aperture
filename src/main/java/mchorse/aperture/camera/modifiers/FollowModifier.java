package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Follow modifier
 * 
 * This modifier is responsible for binding camera to given entity. For 
 * moving fixtures such as paths, follow fixture calculates relative 
 * so the path movement is still possible.
 */
public class FollowModifier extends EntityModifier
{
    @Expose
    public boolean relative;

    @Expose
    public Point offset = new Point(0, 0, 0);

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

        if (fixture != null && this.relative)
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

        pos.point.set(x + this.offset.x, y + this.offset.y, z + this.offset.z);
    }

    @Override
    public AbstractModifier create()
    {
        return new FollowModifier();
    }

    @Override
    public void fromJSON(JsonObject object)
    {
        super.fromJSON(object);

        if (this.offset == null)
        {
            this.offset = new Point(0, 0, 0);
        }
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.relative = buffer.readBoolean();
        this.offset = Point.fromByteBuf(buffer);
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        buffer.writeBoolean(this.relative);
        this.offset.toByteBuf(buffer);
    }
}