package mchorse.aperture.utils.math.functions;

import mchorse.aperture.utils.math.IValue;

public class Cos extends Function
{
    public Cos(IValue[] values) throws Exception
    {
        super(values);
    }

    @Override
    public String getName()
    {
        return "cos";
    }

    @Override
    public int getRequiredArguments()
    {
        return 1;
    }

    @Override
    public double get()
    {
        return Math.cos(this.args[0].get());
    }
}