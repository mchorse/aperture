import java.util.List;

import org.junit.Test;

import mchorse.aperture.utils.math.IValue;
import mchorse.aperture.utils.math.MathBuilder;
import mchorse.aperture.utils.math.Variable;

public class TestMathBuilder
{
    public MathBuilder builder;

    public TestMathBuilder()
    {
        this.builder = new MathBuilder();
        this.builder.variables.put("new", new Variable("new", 25));
    }

    @Test
    public void testBreakdownSymbols()
    {
        List<Object> symbols = this.builder.breakdownChars("2+sin(pi-10-new)-new".split("(?!^)"));

        System.out.println(symbols);
    }

    @Test
    public void testParsing()
    {
        try
        {
            IValue value = this.builder.parse("2+sin(pi - 10 - new)-new");

            System.out.println(value.toString() + " = " + value.get());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}