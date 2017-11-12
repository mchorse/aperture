package mchorse.aperture.camera.data;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Point class
 *
 * This class represents a point in 3 dimensional space. This point class
 * used by {@link Position} class to represent coordinates for fixtures.
 */
public class Point
{
    @Expose
    public float x;

    @Expose
    public float y;

    @Expose
    public float z;

    /**
     * Read a {@link Point} instance from byte buffer 
     */
    public static Point fromByteBuf(ByteBuf buffer)
    {
        return new Point(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public Point(float x, float y, float z)
    {
        this.set(x, y, z);
    }

    public Point(EntityPlayer player)
    {
        this.set(player);
    }

    public void set(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(EntityPlayer player)
    {
        this.set((float) player.posX, (float) player.posY, (float) player.posZ);
    }

    public void toByteBuf(ByteBuf buffer)
    {
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.y);
        buffer.writeFloat(this.z);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).addValue(this.x).addValue(this.y).addValue(this.z).toString();
    }
}