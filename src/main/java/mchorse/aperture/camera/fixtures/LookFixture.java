package mchorse.aperture.camera.fixtures;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.Aperture;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.smooth.Interpolations;
import mchorse.aperture.utils.EntitySelector;
import mchorse.aperture.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Look camera fixture
 *
 * This type of fixture is responsible to transform a camera so it always would
 * be directed towards the given entity.
 */
public class LookFixture extends IdleFixture
{
    protected Entity entity;

    @Expose
    public String selector = "";

    private float oldYaw = 0;
    private float oldPitch = 0;

    public LookFixture(long duration)
    {
        super(duration);
    }

    public Entity getTarget()
    {
        return this.entity;
    }

    @Override
    public void edit(String[] args, EntityPlayer player) throws CommandException
    {
        super.edit(args, player);

        Entity target = EntityUtils.getTargetEntity(player, 64.0);

        if (this.entity == null && target == null)
        {
            throw new CommandException("fixture.no_entity");
        }

        if ((this.entity == null || this.entity.isDead) && target != null)
        {
            this.entity = target;
            this.selector = "@e[type=" + EntityList.getEntityString(target) + "]";
        }
    }

    /**
     * Totally not taken from EntityLookHelper
     */
    @Override
    public void applyFixture(long ticks, float partialTicks, Position pos)
    {
        pos.copy(this.position);

        if (this.entity == null || this.entity.isDead)
        {
            this.tryFindingEntity();
        }

        if (this.entity == null)
        {
            return;
        }

        double x = (this.entity.lastTickPosX + (this.entity.posX - this.entity.lastTickPosX) * partialTicks);
        double y = (this.entity.lastTickPosY + (this.entity.posY - this.entity.lastTickPosY) * partialTicks);
        double z = (this.entity.lastTickPosZ + (this.entity.posZ - this.entity.lastTickPosZ) * partialTicks);

        double dX = x - this.position.point.x;
        double dY = y - this.position.point.y;
        double dZ = z - this.position.point.z;
        double horizontalDistance = MathHelper.sqrt(dX * dX + dZ * dZ);

        float yaw = (float) (MathHelper.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(dY, horizontalDistance) * (180D / Math.PI)));
        float value = Aperture.proxy.config.camera_interpolate_target ? Aperture.proxy.config.camera_interpolate_target_value : 1.0F;

        yaw = Interpolations.lerpYaw(this.oldYaw, yaw, value);
        pitch = Interpolations.lerp(this.oldPitch, pitch, value);

        pos.angle.set(yaw, pitch);

        this.oldYaw = yaw;
        this.oldPitch = pitch;
    }

    @Override
    public void preApplyFixture(long ticks, Position pos)
    {
        this.tryFindingEntity();

        if (this.entity != null)
        {
            double x = this.entity.posX;
            double y = this.entity.posY;
            double z = this.entity.posZ;

            double dX = x - this.position.point.x;
            double dY = y - this.position.point.y;
            double dZ = z - this.position.point.z;
            double horizontalDistance = MathHelper.sqrt(dX * dX + dZ * dZ);

            this.oldYaw = (float) (MathHelper.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
            this.oldPitch = (float) (-(MathHelper.atan2(dY, horizontalDistance) * (180D / Math.PI)));
        }
    }

    /**
     * Try finding entity based on entity selector or target's UUID
     */
    public void tryFindingEntity()
    {
        this.entity = null;

        if (this.selector != null && !this.selector.isEmpty())
        {
            EntityPlayer player = Minecraft.getMinecraft().player;

            try
            {
                this.entity = EntitySelector.matchOneEntity(player, this.selector, Entity.class);
            }
            catch (CommandException e)
            {
                player.sendMessage(new TextComponentTranslation(e.getMessage(), e.getErrorObjects()));
            }
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