package mchorse.aperture.utils.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        /* Remove all spaces, and leading and trailing parenthesis */
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

        return this.parseSymbols(this.breakdownChars(chars));
    }

    public List<Object> breakdownChars(String[] chars)
    {
        List<Object> symbols = new ArrayList<Object>();
        String buffer = "";
        int len = chars.length;

        for (int i = 0; i < len; i++)
        {
            String s = chars[i];

            if (this.isOperator(s))
            {
                if (!buffer.isEmpty())
                {
                    symbols.add(buffer);
                    buffer = "";
                }

                symbols.add(s);
            }
            else if (s.equals("("))
            {
                if (!buffer.isEmpty())
                {
                    symbols.add(buffer);
                    buffer = "";
                }

                int counter = 1;

                for (int j = i + 1; j < len; j++)
                {
                    String c = chars[j];

                    if (c.equals("("))
                    {
                        counter++;
                    }
                    else if (c.equals(")"))
                    {
                        counter--;
                    }

                    if (counter == 0)
                    {
                        symbols.add(this.breakdownChars(buffer.split("(?!^)")));

                        i = j;
                        buffer = "";

                        break;
                    }
                    else
                    {
                        buffer += c;
                    }
                }
            }
            else
            {
                buffer += s;
            }
        }

        if (!buffer.isEmpty())
        {
            symbols.add(buffer);
        }

        return symbols;
    }

    public IValue parseSymbols(List<Object> symbols)
    {
        if (symbols.size() == 1)
        {
            return this.valueFromObject(symbols.get(0));
        }

        Object first = symbols.get(0);
        Object second = symbols.get(1);
        Object third = symbols.get(2);

        if (this.isValue(first) && this.isOperator(second) && this.isValue(third))
        {
            Operation op = this.operationForOperator((String) second);

            if (symbols.size() > 3)
            {
                Object fourth = symbols.get(3);

                if (this.isOperator(fourth))
                {
                    Operation compareTo = this.operationForOperator((String) fourth);

                    if (compareTo.value > op.value)
                    {
                        return new Operator(op, this.valueFromObject(first), this.parseSymbols(symbols.subList(2, symbols.size())));
                    }
                    else
                    {
                        return new Operator(compareTo, new Operator(op, this.valueFromObject(first), this.valueFromObject(third)), this.parseSymbols(symbols.subList(4, symbols.size())));
                    }
                }
            }
            else
            {
                return new Operator(op, this.valueFromObject(first), this.valueFromObject(third));
            }
        }

        System.out.println(symbols);

        return null;
    }

    public IValue valueFromObject(Object object)
    {
        if (object instanceof String)
        {
            String symbol = (String) object;

            if (this.isDecimal(symbol))
            {
                return new Constant(Double.parseDouble(symbol));
            }
            else if (this.values.containsKey(symbol))
            {
                return this.values.get(symbol);
            }
        }
        else if (object instanceof List)
        {
            return this.parseSymbols((List<Object>) object);
        }

        return null;
    }

    private boolean isValue(Object o)
    {
        return o instanceof String && !this.isOperator((String) o) || o instanceof List;
    }

    private boolean isOperator(Object o)
    {
        return o instanceof String && this.isOperator((String) o);
    }

    private boolean isOperator(String s)
    {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("%") || s.equals("^");
    }

    private Operation operationForOperator(String op)
    {
        for (Operation operation : Operation.values())
        {
            if (operation.sign.equals(op))
            {
                return operation;
            }
        }

        return null;
    }

    private boolean isDecimal(String s)
    {
        return s.matches("^-?\\d+(\\.\\d+)?$");
    }
}