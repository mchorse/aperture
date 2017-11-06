package mchorse.aperture.utils.math.functions;

import mchorse.aperture.utils.math.IValue;

/**
 * Function class
 */
public abstract class Function implements IValue
{
    protected IValue[] args;

    public Function(IValue[] values)
    {
        this.args = values;
    }

    @Override
    public String toString()
    {
        String args = "";

        for (int i = 0; i < this.args.length; i++)
        {
            args += this.args[i].toString();

            if (i < this.args.length - 1)
            {
                args += ", ";
            }
        }

        return this.getName() + "(" + args + ")";
    }

    public abstract String getName();

    public int getRequiredArguments()
    {
        return 0;
    }
}