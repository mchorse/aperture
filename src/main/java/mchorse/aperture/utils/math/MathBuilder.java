package mchorse.aperture.utils.math;

import java.util.HashMap;
import java.util.Map;

public class MathBuilder
{
    public Map<String, IValue> values = new HashMap<String, IValue>();

    public MathBuilder()
    {
        /* Some default values */
        this.values.put("pi", new Variable("pi", Math.PI));
    }

    public IValue parse(String string)
    {
        /* If given string have illegal characters, then it can't be parsed */
        if (!string.matches("^[\\w\\d\\s_+-/*%^.,()]+$"))
        {
            return null;
        }

        string = string.replaceAll("\\s+", "");
        String[] chars = string.split("(?!^)");

        int left = 0;
        int right = 0;

        for (String s : chars)
        {
            if (s.equals("("))
            {
                left++;
            }
            else if (s.equals(")"))
            {
                right++;
            }
        }

        /* Amount of left and right brackets should be the same */
        if (left != right)
        {
            return null;
        }

        return parseChars(chars);
    }

    public IValue parseChars(String[] chars)
    {
        String buffer = "";

        return null;
    }

    private boolean isOperator(String s)
    {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("%") || s.equals("^");
    }
}