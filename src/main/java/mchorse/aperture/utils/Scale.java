package mchorse.aperture.utils;

import net.minecraft.util.math.MathHelper;

/**
 * This class represents a scale of an axis 
 */
public class Scale
{
    public double shift = 0;
    public double zoom = 1;
    public int mult = 1;
    public boolean inverse;

    public Scale(boolean inverse)
    {
        this.inverse = inverse;
    }

    public void set(double shift, double zoom)
    {
        this.shift = shift;
        this.zoom = zoom;
    }

    public double to(double x)
    {
        return (!this.inverse ? x - this.shift : -x + this.shift) * this.zoom;
    }

    public double from(double x)
    {
        return this.inverse ? -(x / this.zoom - this.shift) : x / this.zoom + this.shift;
    }

    public void view(double min, double max, double width)
    {
        this.view(min, max, width, 0);
    }

    public void view(double min, double max, double length, double offset)
    {
        this.zoom = 1 / ((max - min) / length);

        if (offset != 0)
        {
            min -= offset * (1 / this.zoom);
            max += offset * (1 / this.zoom);
            this.zoom = 1 / ((max - min) / length);
        }

        this.shift = (max + min) / 2F;
    }

    public void zoom(double amount, double min, double max)
    {
        this.zoom += amount;
        this.zoom = MathHelper.clamp(this.zoom, min, max);
    }
}