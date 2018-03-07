package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonObject;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.utils.math.IValue;
import mchorse.aperture.utils.math.MathBuilder;
import mchorse.aperture.utils.math.Variable;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Math modifier
 * 
 * Probably the most complex modifier in Aperture. This modifier accepts 
 * a math expression (which supports basic operators, variables and 
 * functions) written by user, and calculates the value based on that 
 * expression.
 * 
 * This modifier provides all essential input variables for math 
 * expressions, such as: position, angle, progress, progress offset from 
 * fixture, current value and more!
 */
public class MathModifier extends ComponentModifier
{
    public IValue expression;
    public MathBuilder builder = new MathBuilder();

    public Variable ticks;
    public Variable offset;
    public Variable partial;
    public Variable fixtureDuration;
    public Variable progress;
    public Variable value;

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
        this.offset = new Variable("o", 0);
        this.partial = new Variable("pt", 0);
        this.fixtureDuration = new Variable("d", 0);
        this.progress = new Variable("p", 0);
        this.value = new Variable("value", 0);

        this.x = new Variable("x", 0);
        this.y = new Variable("y", 0);
        this.z = new Variable("z", 0);

        this.yaw = new Variable("yaw", 0);
        this.pitch = new Variable("pitch", 0);
        this.roll = new Variable("roll", 0);
        this.fov = new Variable("fov", 0);

        this.builder.register(this.ticks);
        this.builder.register(this.offset);
        this.builder.register(this.partial);
        this.builder.register(this.fixtureDuration);
        this.builder.register(this.progress);
        this.builder.register(this.value);

        this.builder.register(this.x);
        this.builder.register(this.y);
        this.builder.register(this.z);

        this.builder.register(this.yaw);
        this.builder.register(this.pitch);
        this.builder.register(this.roll);
        this.builder.register(this.fov);
    }

    public MathModifier(String expression)
    {
        this();
        this.rebuildExpression(expression);
    }

    public boolean rebuildExpression(String expression)
    {
        try
        {
            this.expression = this.builder.parse(expression);

            return true;
        }
        catch (Exception e)
        {}

        return false;
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, Position pos)
    {
        if (this.expression != null)
        {
            this.ticks.set(ticks);
            this.offset.set(offset);
            this.partial.set(partialTick);
            this.fixtureDuration.set(fixture.getDuration());
            this.progress.set(ticks + partialTick);

            this.x.set(pos.point.x);
            this.y.set(pos.point.y);
            this.z.set(pos.point.z);

            this.yaw.set(pos.angle.yaw);
            this.pitch.set(pos.angle.pitch);
            this.roll.set(pos.angle.roll);
            this.fov.set(pos.angle.fov);

            if (this.isActive(0))
            {
                this.value.set(pos.point.x);
                pos.point.x = (float) this.expression.get();
            }

            if (this.isActive(1))
            {
                this.value.set(pos.point.y);
                pos.point.y = (float) this.expression.get();
            }

            if (this.isActive(2))
            {
                this.value.set(pos.point.z);
                pos.point.z = (float) this.expression.get();
            }

            if (this.isActive(3))
            {
                this.value.set(pos.angle.yaw);
                pos.angle.yaw = (float) this.expression.get();
            }

            if (this.isActive(4))
            {
                this.value.set(pos.angle.pitch);
                pos.angle.pitch = (float) this.expression.get();
            }

            if (this.isActive(5))
            {
                this.value.set(pos.angle.roll);
                pos.angle.roll = (float) this.expression.get();
            }

            if (this.isActive(6))
            {
                this.value.set(pos.angle.fov);
                pos.angle.fov = (float) this.expression.get();
            }
        }
    }

    @Override
    public AbstractModifier clone()
    {
        MathModifier modifier = new MathModifier();

        modifier.enabled = this.enabled;
        modifier.active = this.active;

        if (this.expression != null)
        {
            modifier.rebuildExpression(this.expression.toString());
        }

        return modifier;
    }

    @Override
    public void toJSON(JsonObject object)
    {
        if (this.expression != null)
        {
            object.addProperty("expression", this.expression.toString());
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

        ByteBufUtils.writeUTF8String(buffer, this.expression == null ? "" : this.expression.toString());
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.rebuildExpression(ByteBufUtils.readUTF8String(buffer));
    }
}