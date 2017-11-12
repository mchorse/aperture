package mchorse.aperture.utils.math.functions;

import mchorse.aperture.utils.math.IValue;

/**
 * Abstract function class
 * 
 * This class provides function capability (i.e. giving it arguments and 
 * upon {@link #get()} method you receive output).
 */
public abstract class Function implements IValue
{
    protected IValue[] args;

    public Function(IValue[] values) throws Exception
    {
        if (values.length < this.getRequiredArguments())
        {
            String message = String.format("Function '%s' requires at least %s arguments. %s are given!", this.getName(), this.getRequiredArguments(), values.length);

            throw new Exception(message);
        }

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

    /**
     * Get name of this function 
     */
    public abstract String getName();

    /**
     * Get minimum count of arguments this function needs
     */
    public int getRequiredArguments()
    {
        return 0;
    }
}