package mchorse.aperture.utils.math;

public enum Operation
{
    ADD("+", 1)
    {
        @Override
        public double calculate(double a, double b)
        {
            return a + b;
        }
    },
    SUB("-", 1)
    {
        @Override
        public double calculate(double a, double b)
        {
            return a - b;
        }
    },
    MUL("*", 2)
    {
        @Override
        public double calculate(double a, double b)
        {
            return a * b;
        }
    },
    DIV("/", 2)
    {
        @Override
        public double calculate(double a, double b)
        {
            /* To avoid any exceptions */
            return a / (b == 0 ? 1 : b);
        }
    },
    MOD("%", 2)
    {
        @Override
        public double calculate(double a, double b)
        {
            return a % b;
        }
    },
    POW("^", 3)
    {
        @Override
        public double calculate(double a, double b)
        {
            return Math.pow(a, b);
        }
    };

    public final String sign;
    public final int value;

    private Operation(String sign, int value)
    {
        this.sign = sign;
        this.value = value;
    }

    public abstract double calculate(double a, double b);
}
