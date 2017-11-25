package mchorse.aperture.utils.math;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mchorse.aperture.utils.math.functions.Abs;
import mchorse.aperture.utils.math.functions.Clamp;
import mchorse.aperture.utils.math.functions.Cos;
import mchorse.aperture.utils.math.functions.Floor;
import mchorse.aperture.utils.math.functions.Function;
import mchorse.aperture.utils.math.functions.Sin;
import net.minecraft.util.math.MathHelper;

/**
 * Math builder
 * 
 * This class is responsible for parsing math expressions provided by 
 * user in a string to an {@link IValue} which can be used to compute 
 * some value dynamically using different math operators, variables and 
 * functions.
 * 
 * It works by first breaking down given string into a list of tokens 
 * and then putting them together in a binary tree-like {@link IValue}.
 * 
 * TODO: maybe implement constant pool (to reuse same values)?
 * TODO: maybe pre-compute constant expressions?
 */
public class MathBuilder
{
    /**
     * Named variables that can be used in math expression by this 
     * builder
     */
    public Map<String, IValue> variables = new HashMap<String, IValue>();

    /**
     * Map of functions which can be used in the math expressions
     */
    public Map<String, Class<? extends Function>> functions = new HashMap<String, Class<? extends Function>>();

    public MathBuilder()
    {
        /* Some default values */
        this.register(new Variable("PI", Math.PI));
        this.register(new Variable("E", Math.E));

        /* Some default functions */
        this.functions.put("abs", Abs.class);
        this.functions.put("clamp", Clamp.class);
        this.functions.put("cos", Cos.class);
        this.functions.put("floor", Floor.class);
        this.functions.put("sin", Sin.class);
    }

    /**
     * Register a variable 
     */
    public void register(Variable var)
    {
        this.variables.put(var.getName(), var);
    }

    /**
     * Parse given math expression into a {@link IValue} which can be 
     * used to execute math.
     */
    public IValue parse(String expression) throws Exception
    {
        /* If given string have illegal characters, then it can't be parsed */
        if (!expression.matches("^[\\w\\d\\s_+-/*%^.,()]+$"))
        {
            throw new Exception("Given expression '" + expression + "' contains illegal characters!");
        }

        /* Remove all spaces, and leading and trailing parenthesis */
        expression = expression.replaceAll("\\s+", "");

        if (expression.startsWith("(") && expression.endsWith(")"))
        {
            expression = expression.replaceAll("^\\(", "").replaceAll("\\)$", "");
        }

        String[] chars = expression.split("(?!^)");

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
            throw new Exception("Given expression '" + expression + "' has more uneven amount of parenthesis, there are " + left + " open and " + right + " closed!");
        }

        return this.parseSymbols(this.breakdownChars(chars));
    }

    /**
     * Breakdown characters into a list of math expression symbols. 
     */
    public List<Object> breakdownChars(String[] chars)
    {
        List<Object> symbols = new ArrayList<Object>();
        String buffer = "";
        int len = chars.length;

        for (int i = 0; i < len; i++)
        {
            String s = chars[i];

            if (this.isOperator(s) || s.equals(","))
            {
                /* Taking care of a special case of using minus sign to 
                 * invert the positive value */
                if (s.equals("-"))
                {
                    int size = symbols.size();

                    boolean isFirst = size == 0 && buffer.isEmpty();
                    boolean isOperatorBehind = size > 0 && (this.isOperator(symbols.get(size - 1)) || symbols.get(size - 1).equals(",")) && buffer.isEmpty();

                    if (isFirst || isOperatorBehind)
                    {
                        buffer += s;

                        continue;
                    }
                }

                /* Push buffer and operator */
                if (!buffer.isEmpty())
                {
                    symbols.add(buffer);
                    buffer = "";
                }

                symbols.add(s);
            }
            else if (s.equals("("))
            {
                /* Push a list of symbols */
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
                /* Accumulate the buffer */
                buffer += s;
            }
        }

        if (!buffer.isEmpty())
        {
            symbols.add(buffer);
        }

        return symbols;
    }

    /**
     * Parse symbols
     * 
     * This function is the most important part of this class. It's 
     * responsible for turning list of symbols into {@link IValue}. This 
     * is done by constructing a binary tree-like {@link IValue} based on 
     * {@link Operator} class.
     * 
     * However, beside parsing operations, it's also can return one or 
     * two item sized symbol lists.
     */
    public IValue parseSymbols(List<Object> symbols) throws Exception
    {
        int size = symbols.size();

        /* Constant, variable or group (parenthesis) */
        if (size == 1)
        {
            return this.valueFromObject(symbols.get(0));
        }

        /* Function */
        if (size == 2)
        {
            Object first = symbols.get(0);
            Object second = symbols.get(1);

            if (this.isVariable(first) && second instanceof List)
            {
                return this.createFunction((String) first, (List<Object>) second);
            }
        }

        /* Any other math expression */
        int firstOp = -1;
        int secondOp = -1;

        /* Find next two operators' indices */
        for (int i = 0; i < size; i++)
        {
            Object o = symbols.get(i);

            if (this.isOperator(o))
            {
                if (firstOp == -1)
                {
                    firstOp = i;
                }
                else
                {
                    secondOp = i;
                    break;
                }
            }
        }

        /* And finally construct the math operation */
        Operation op = this.operationForOperator((String) symbols.get(firstOp));

        if (secondOp == -1)
        {
            IValue left = this.parseSymbols(symbols.subList(0, firstOp));
            IValue right = this.parseSymbols(symbols.subList(firstOp + 1, MathHelper.clamp_int(firstOp + 3, 0, size)));

            return new Operator(op, left, right);
        }
        else if (secondOp > firstOp)
        {
            Operation compareTo = this.operationForOperator((String) symbols.get(secondOp));
            IValue left = this.parseSymbols(symbols.subList(0, firstOp));

            if (compareTo.value > op.value)
            {
                return new Operator(op, left, this.parseSymbols(symbols.subList(firstOp + 1, size)));
            }
            else
            {
                IValue right = this.parseSymbols(symbols.subList(firstOp + 1, secondOp));

                return new Operator(compareTo, new Operator(op, left, right), this.parseSymbols(symbols.subList(secondOp + 1, size)));
            }
        }

        throw new Exception("Given symbols couldn't be parsed! " + symbols);
    }

    /**
     * Create a function value
     * 
     * This method in comparison to {@link #valueFromObject(Object)} 
     * needs the name of the function and list of args (which can't be 
     * stored in one object).
     * 
     * This method will constructs {@link IValue}s from list of args 
     * mixed with operators, groups, values and commas. And then plug it 
     * in to a class constructor with given name. 
     */
    private IValue createFunction(String first, List<Object> args) throws Exception
    {
        if (!this.functions.containsKey(first))
        {
            throw new Exception("Function '" + first + "' couldn't be found!");
        }

        List<IValue> values = new ArrayList<IValue>();
        List<Object> buffer = new ArrayList<Object>();

        for (Object o : args)
        {
            if (o.equals(","))
            {
                values.add(this.parseSymbols(buffer));
                buffer.clear();
            }
            else
            {
                buffer.add(o);
            }
        }

        if (!buffer.isEmpty())
        {
            values.add(this.parseSymbols(buffer));
        }

        Class<? extends Function> function = this.functions.get(first);
        Constructor<? extends Function> ctor = function.getConstructor(IValue[].class);
        Function func = ctor.newInstance(new Object[] {values.toArray(new IValue[values.size()])});

        return func;
    }

    /**
     * Get value from an object.
     * 
     * This method is responsible for creating different sort of values 
     * based on the input object. It can create constants, variables and 
     * groups. 
     */
    public IValue valueFromObject(Object object) throws Exception
    {
        if (object instanceof String)
        {
            String symbol = (String) object;

            if (this.isDecimal(symbol))
            {
                return new Constant(Double.parseDouble(symbol));
            }
            else if (this.isVariable(symbol))
            {
                /* Need to account for a negative value variable */
                if (symbol.indexOf("-") == 0)
                {
                    symbol = symbol.substring(1);
                    IValue value = this.variables.get(symbol);

                    if (value instanceof Variable)
                    {
                        return ((Variable) value).negative;
                    }
                }
                else
                {
                    return this.variables.get(symbol);
                }
            }
        }
        else if (object instanceof List)
        {
            return new Group(this.parseSymbols((List<Object>) object));
        }

        throw new Exception("Given object couldn't be converted to value! " + object);
    }

    /**
     * Get operation for given operator strings 
     */
    private Operation operationForOperator(String op) throws Exception
    {
        for (Operation operation : Operation.values())
        {
            if (operation.sign.equals(op))
            {
                return operation;
            }
        }

        throw new Exception("There is no such operator '" + op + "'!");
    }

    /**
     * Whether given object is a variable 
     */
    private boolean isVariable(Object o)
    {
        return o instanceof String && !this.isDecimal((String) o) && !this.isOperator((String) o);
    }

    /**
     * Whether given object is a value (it can be a constant, variable 
     * or a group) 
     */
    private boolean isValue(Object o)
    {
        return o instanceof String && !this.isOperator((String) o) || o instanceof List;
    }

    private boolean isOperator(Object o)
    {
        return o instanceof String && this.isOperator((String) o);
    }

    /**
     * Whether string is an operator 
     */
    private boolean isOperator(String s)
    {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("%") || s.equals("^");
    }

    /**
     * Whether string is numeric (including whether it's a floating 
     * number) 
     */
    private boolean isDecimal(String s)
    {
        return s.matches("^-?\\d+(\\.\\d+)?$");
    }
}