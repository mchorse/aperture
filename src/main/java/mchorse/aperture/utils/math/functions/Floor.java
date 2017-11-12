package mchorse.aperture.utils.math.functions;

import mchorse.aperture.utils.math.IValue;

public class Floor extends Function
{
    public Floor(IValue[] values) throws Exception
    {
        super(values);
    }

    @Override
    public String getName()
    {
        return "floor";
    }

    @Override
    public int getRequiredArguments()
    {
        return 1;
    }

    @Override
    public double get()
    {
        return Math.floor(this.args[0].get());
    }
}