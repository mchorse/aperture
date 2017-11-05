package mchorse.aperture.utils.math;

public enum Operation
{
    ADD("+")
    {
        @Override
        public double calculate(double a, double b)
        {
            return a + b;
        }
    },
    SUB("-")
    {
        @Override
        public double calculate(double a, double b)
        {
            return a - b;
        }
    },
    MUL("*")
    {
        @Override
        public double calculate(double a, double b)
        {
            return a * b;
        }
    },
    DIV("/")
    {
        @Override
        public double calculate(double a, double b)
        {
            /* To avoid any exceptions */
            return a / (b == 0 ? 1 : b);
        }
    },
    MOD("%")
    {
        @Override
        public double calculate(double a, double b)
        {
            return a % b;
        }
    },
    POW("^")
    {
        @Override
        public double calculate(double a, double b)
        {
            return Math.pow(a, b);
        }
    };

    public String sign;

    private Operation(String sign)
    {
        this.sign = sign;
    }

    public abstract double calculate(double a, double b);
}
