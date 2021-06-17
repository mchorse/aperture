package mchorse.aperture.camera.data;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonObject;
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
    public double x;
    public double y;
    public double z;

    /**
     * Read a {@link Point} instance from byte buffer 
     */
    public static Point fromBytes(ByteBuf buffer)
    {
        return new Point(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public Point(double x, double y, double z)
    {
        this.set(x, y, z);
    }

    public Point(EntityPlayer player)
    {
        this.set(player);
    }

    public void set(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Point point)
    {
        this.set(point.x, point.y, point.z);
    }

    public void set(EntityPlayer player)
    {
        this.set(player.posX, player.posY + player.getEyeHeight(), player.posZ);
    }

    public void fromJSON(JsonObject element)
    {
        this.x = element.get("x").getAsDouble();
        this.y = element.get("y").getAsDouble();
        this.z = element.get("z").getAsDouble();
    }

    public JsonObject toJSON()
    {
        JsonObject object = new JsonObject();

        object.addProperty("x", this.x);
        object.addProperty("y", this.y);
        object.addProperty("z", this.z);

        return object;
    }

    public void toBytes(ByteBuf buffer)
    {
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
    }

    public Point copy()
    {
        return new Point(this.x, this.y, this.z);
    }

    public double length(Point point)
    {
        double dx = point.x - this.x;
        double dy = point.y - this.y;
        double dz = point.z - this.z;

        return Math.sqrt(dx * dx  + dy * dy + dz * dz);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Point)
        {
            Point point = (Point) obj;

            return this.x == point.x && this.y == point.y && this.z == point.z;
        }

        return super.equals(obj);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this).addValue(this.x).addValue(this.y).addValue(this.z).toString();
    }
}