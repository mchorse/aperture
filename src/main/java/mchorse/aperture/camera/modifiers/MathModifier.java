package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonObject;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.utils.math.IValue;
import mchorse.aperture.utils.math.MathBuilder;
import mchorse.aperture.utils.math.Variable;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MathModifier extends AbstractModifier
{
    public IValue value;
    public MathBuilder builder = new MathBuilder();

    public Variable ticks;
    public Variable partial;

    public Variable x;
    public Variable y;
    public Variable z;

    public Variable yaw;
    public Variable pitch;
    public Variable roll;
    public Variable fov;

    public MathModifier()
    {
        this.ticks = new Variable("t", 0);
        this.partial = new Variable("pt", 0);

        this.x = new Variable("x", 0);
        this.y = new Variable("y", 0);
        this.z = new Variable("z", 0);

        this.yaw = new Variable("yaw", 0);
        this.pitch = new Variable("pitch", 0);
        this.roll = new Variable("roll", 0);
        this.fov = new Variable("fov", 0);

        this.builder.variables.put("t", this.ticks);
        this.builder.variables.put("pt", this.partial);

        this.builder.variables.put("x", this.x);
        this.builder.variables.put("y", this.y);
        this.builder.variables.put("z", this.z);

        this.builder.variables.put("yaw", this.yaw);
        this.builder.variables.put("pitch", this.pitch);
        this.builder.variables.put("roll", this.roll);
        this.builder.variables.put("fov", this.fov);
    }

    public MathModifier(String expression)
    {
        this();
        this.rebuildExpression(expression);
    }

    public void rebuildExpression(String expression)
    {
        try
        {
            IValue value = this.builder.parse(expression);

            if (value != null)
            {
                this.value = value;
            }
        }
        catch (Exception e)
        {}
    }

    @Override
    public void modify(long ticks, AbstractFixture fixture, float partialTick, Position pos)
    {
        if (this.value != null)
        {
            this.ticks.set(ticks);
            this.partial.set(partialTick);

            this.x.set(pos.point.x);
            this.y.set(pos.point.y);
            this.z.set(pos.point.z);

            this.yaw.set(pos.angle.yaw);
            this.pitch.set(pos.angle.pitch);
            this.roll.set(pos.angle.roll);
            this.fov.set(pos.angle.fov);

            pos.point.y = (float) this.value.get();
        }
    }

    @Override
    public void toJSON(JsonObject object)
    {
        if (this.value != null)
        {
            object.addProperty("expression", this.value.toString());
        }
    }

    @Override
    public void fromJSON(JsonObject object)
    {
        if (object.has("expression"))
        {
            this.rebuildExpression(object.get("expression").getAsString());
        }
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        ByteBufUtils.writeUTF8String(buffer, this.value == null ? "" : this.value.toString());
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.rebuildExpression(ByteBufUtils.readUTF8String(buffer));
    }

    @Override
    public byte getType()
    {
        return AbstractModifier.MATH;
    }
}