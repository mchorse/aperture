package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.math.IValue;
import mchorse.mclib.math.MathBuilder;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class ValueExpression extends Value
{
    public IValue expression;
    public MathBuilder builder;
    public boolean lastError;

    public ValueExpression(String id, MathBuilder builder)
    {
        super(id);

        this.builder = builder;
    }

    public boolean isErrored()
    {
        return this.lastError;
    }

    public IValue get()
    {
        return this.expression;
    }

    public void set(String expression) throws Exception
    {
        this.expression = this.builder.parse(expression);
    }

    @Override
    public Object getValue()
    {
        return this.toString();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof String)
        {
            try
            {
                String string = (String) object;

                if (string.isEmpty())
                {
                    this.expression = null;
                }
                else
                {
                    this.set((String) object);
                }

                this.lastError = false;
            }
            catch (Exception e)
            {
                this.expression = null;
                this.lastError = true;
            }
        }
    }

    @Override
    public void reset()
    {
        this.expression = null;
        this.lastError = false;
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueExpression)
        {
            this.setValue(value.getValue());
        }
    }

    @Override
    public void valueFromJSON(JsonElement element)
    {
        if (element.isJsonPrimitive())
        {
            this.setValue(element.getAsString());
        }
    }

    @Override
    public JsonElement valueToJSON()
    {
        return new JsonPrimitive(this.expression == null ? "" : this.expression.toString());
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.setValue(ByteBufUtils.readUTF8String(buffer));
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        ByteBufUtils.writeUTF8String(buffer, (String) this.getValue());
    }

    @Override
    public String toString()
    {
        return this.expression == null ? "" : this.expression.toString();
    }
}
