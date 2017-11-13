package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.utils.EntitySelector;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Abstract entity modifier
 * 
 * Abstract class for any new modifiers which are going to use entity 
 * selector to fetch an entity and apply some modifications to the path 
 * based on the entity.
 */
public abstract class EntityModifier extends AbstractModifier
{
    /**
     * Position which may be used for calculation of relative 
     * camera fixture animations
     */
    public Position position = new Position(0, 0, 0, 0, 0);

    /**
     * Target entity 
     */
    public Entity entity;

    /**
     * Target (entity) selector
     * 
     * @link https://minecraft.gamepedia.com/Commands#Target_selector_variables
     */
    @Expose
    public String selector = "";

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
                e.printStackTrace();
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