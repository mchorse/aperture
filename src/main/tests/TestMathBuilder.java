import java.util.List;

import org.junit.Test;

import mchorse.aperture.utils.math.IValue;
import mchorse.aperture.utils.math.MathBuilder;

public class TestMathBuilder
{
    public MathBuilder builder;

    public TestMathBuilder()
    {
        this.builder = new MathBuilder();
    }

    @Test
    public void testBreakdownSymbols()
    {
        List<Object> symbols = this.builder.breakdownChars("pow(2+2,5)-10*pi".split("(?!^)"));

        System.out.println(symbols);
    }

    @Test
    public void testParsing()
    {
        IValue value = this.builder.parse("-5 + 10");

        System.out.println(value.toString() + " = " + value.get());
    }
}