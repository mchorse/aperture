package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.utils.EntitySelector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Iterator;
import java.util.List;

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
    public List<Entity> entities;

    /**
     * Target (entity) selector
     * 
     * @link https://minecraft.gamepedia.com/Commands#Target_selector_variables
     */
    @Expose
    public String selector = "";

    @Expose
    public Point offset = new Point(0, 0, 0);

    /**
     * Try finding entity based on entity selector or target's UUID
     */
    public void tryFindingEntity()
    {
        this.entities = null;

        if (this.selector != null && !this.selector.isEmpty())
        {
            String selector = this.selector;
            EntityPlayer player = Minecraft.getMinecraft().player;

            if (!this.selector.contains("@"))
            {
                selector = "@e[name=" + selector + "]";
            }

            try
            {
                this.entities = EntitySelector.matchEntities(player, selector, Entity.class);

                if (this.entities.isEmpty())
                {
                    this.entities = null;
                }
            }
            catch (Exception e)
            {
                this.entities = null;
            }
        }
    }

    /**
     * Check for dead entities
     */
    protected boolean checkForDead()
    {
        if (this.entities == null)
        {
            return true;
        }

        Iterator<Entity> it = this.entities.iterator();

        while (it.hasNext())
        {
            Entity entity = it.next();

            if (entity.isDead || entity == Minecraft.getMinecraft().player)
            {
                it.remove();
            }
        }

        if (this.entities.isEmpty())
        {
            this.entities = null;
        }

        return this.entities == null;
    }

    /* Save/load methods */

    @Override
    public void copy(AbstractModifier from)
    {
        super.copy(from);

        if (from instanceof EntityModifier)
        {
            this.selector = ((EntityModifier) from).selector;
            this.offset.set(((EntityModifier) from).offset);
        }
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
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.selector = ByteBufUtils.readUTF8String(buffer);
        this.offset = Point.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        ByteBufUtils.writeUTF8String(buffer, this.selector);
        this.offset.toBytes(buffer);
    }
}