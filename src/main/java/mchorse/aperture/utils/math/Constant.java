package mchorse.aperture.utils.math;

public class Constant implements IValue
{
    private double value;

    public Constant(double value)
    {
        this.value = value;
    }

    @Override
    public double get()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }
}