package mchorse.aperture.utils;

import net.minecraft.util.math.MathHelper;

/**
 * This class represents a scale of an axis 
 */
public class Scale
{
    public float shift = 0;
    public float zoom = 1;
    public int mult = 1;
    public boolean inverse;

    public Scale(boolean inverse)
    {
        this.inverse = inverse;
    }

    public void set(float shift, float zoom)
    {
        this.shift = shift;
        this.zoom = zoom;
    }

    public float to(float x)
    {
        return (!this.inverse ? x - this.shift : -x + this.shift) * this.zoom;
    }

    public float from(float x)
    {
        return this.inverse ? -(x / this.zoom - this.shift) : x / this.zoom + this.shift;
    }

    public void view(float min, float max, float width)
    {
        this.view(min, max, width, 0);
    }

    public void view(float min, float max, float length, float offset)
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

    public void zoom(float amount, float min, float max)
    {
        this.zoom += amount;
        this.zoom = MathHelper.clamp(this.zoom, min, max);
    }
}