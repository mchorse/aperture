package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.utils.EntitySelector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class LookModifier extends AbstractModifier
{
    public Entity entity;

    @Expose
    public String selector = "";

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

        double x = (this.entity.lastTickPosX + (this.entity.posX - this.entity.lastTickPosX) * partialTick);
        double y = (this.entity.lastTickPosY + (this.entity.posY - this.entity.lastTickPosY) * partialTick);
        double z = (this.entity.lastTickPosZ + (this.entity.posZ - this.entity.lastTickPosZ) * partialTick);

        double dX = x - pos.point.x;
        double dY = y - pos.point.y;
        double dZ = z - pos.point.z;
        double horizontalDistance = MathHelper.sqrt_double(dX * dX + dZ * dZ);

        float yaw = (float) (MathHelper.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(dY, horizontalDistance) * (180D / Math.PI)));

        pos.angle.set(yaw, pitch);
    }

    /**
     * Try finding entity based on entity selector or target's UUID
     */
    public void tryFindingEntity()
    {
        this.entity = null;

        if (this.selector != null && !this.selector.isEmpty())
        {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            this.entity = EntitySelector.matchOneEntity(player, this.selector, Entity.class);
        }
    }

    /* Save/load methods */

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.selector = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        ByteBufUtils.writeUTF8String(buffer, this.selector);
    }
}