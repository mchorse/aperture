package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.values.ValuePoint;
import mchorse.aperture.utils.EntitySelector;
import mchorse.mclib.config.values.ValueString;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public final ValueString selector = new ValueString("selector", "");
    public final ValuePoint offset = new ValuePoint("offset", new Point(0, 0, 0));

    public EntityModifier()
    {
        super();

        this.register(this.selector);
        this.register(this.offset);
    }

    /**
     * Try finding entity based on entity selector or target's UUID
     */
    public void tryFindingEntity()
    {
        this.entities = null;

        String selector = this.selector.get();

        if (selector != null && !selector.isEmpty() && FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            this.tryFindingEntityClient(selector);
        }
    }

    @SideOnly(Side.CLIENT)
    private void tryFindingEntityClient(String selector)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (!selector.contains("@"))
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
}