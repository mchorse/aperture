package mchorse.aperture.camera.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.values.ValueExpression;
import mchorse.mclib.math.IValue;
import mchorse.mclib.math.MathBuilder;
import mchorse.mclib.math.Variable;

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
    private static Position current = new Position();
    private static Position position = new Position();

    public MathBuilder builder = new MathBuilder();

    public Variable ticks;
    public Variable offset;
    public Variable partial;
    public Variable duration;
    public Variable progress;
    public Variable factor;
    public Variable velocity;

    public Variable value;

    public Variable x;
    public Variable y;
    public Variable z;

    public Variable yaw;
    public Variable pitch;
    public Variable roll;
    public Variable fov;

    public final ValueExpression expression = new ValueExpression("expression", this.builder);

    public MathModifier()
    {
        super();

        this.register(this.expression);

        this.ticks = new Variable("t", 0);
        this.offset = new Variable("o", 0);
        this.partial = new Variable("pt", 0);
        this.duration = new Variable("d", 0);
        this.progress = new Variable("p", 0);
        this.factor = new Variable("f", 0);
        this.velocity = new Variable("v", 0);

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
        this.builder.register(this.duration);
        this.builder.register(this.progress);
        this.builder.register(this.factor);
        this.builder.register(this.velocity);

        this.builder.register(this.value);

        this.builder.register(this.x);
        this.builder.register(this.y);
        this.builder.register(this.z);

        this.builder.register(this.yaw);
        this.builder.register(this.pitch);
        this.builder.register(this.roll);
        this.builder.register(this.fov);
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        IValue expression = this.expression.get();

        if (expression != null)
        {
            int step = 1;

            if (fixture != null)
            {
                fixture.applyFixture(offset + step, partialTick, previewPartialTick, profile, position);
                AbstractModifier.applyModifiers(profile, fixture, ticks + step, offset + step, partialTick, previewPartialTick, this, position);

                fixture.applyFixture(offset, partialTick, previewPartialTick, profile, current);
                AbstractModifier.applyModifiers(profile, fixture, ticks, offset, partialTick, previewPartialTick, this, current);
            }
            else
            {
                profile.applyProfile(ticks + step, partialTick, previewPartialTick, position, true, this);
                profile.applyProfile(ticks, partialTick, previewPartialTick, current, true, this);
            }

            double dx = current.point.x - position.point.x;
            double dy = current.point.y - position.point.y;
            double dz = current.point.z - position.point.z;
            double velocity = Math.sqrt(dx * dx + dy * dy + dz * dz);

            this.velocity.set(velocity);

            this.ticks.set(ticks);
            this.offset.set(offset);
            this.partial.set(previewPartialTick);
            this.duration.set(fixture == null ? profile.getDuration() : fixture.getDuration());
            this.progress.set(ticks + previewPartialTick);
            this.factor.set((double) (offset + previewPartialTick) / this.duration.get().doubleValue());

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
                pos.point.x = expression.get().doubleValue();
            }

            if (this.isActive(1))
            {
                this.value.set(pos.point.y);
                pos.point.y = expression.get().doubleValue();
            }

            if (this.isActive(2))
            {
                this.value.set(pos.point.z);
                pos.point.z = expression.get().doubleValue();
            }

            if (this.isActive(3))
            {
                this.value.set(pos.angle.yaw);
                pos.angle.yaw = (float) expression.get().doubleValue();
            }

            if (this.isActive(4))
            {
                this.value.set(pos.angle.pitch);
                pos.angle.pitch = (float) expression.get().doubleValue();
            }

            if (this.isActive(5))
            {
                this.value.set(pos.angle.roll);
                pos.angle.roll = (float) expression.get().doubleValue();
            }

            if (this.isActive(6))
            {
                this.value.set(pos.angle.fov);
                pos.angle.fov = (float) expression.get().doubleValue();
            }
        }
    }

    @Override
    public AbstractModifier create()
    {
        return new MathModifier();
    }
}