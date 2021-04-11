package mchorse.aperture.camera.modifiers;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.values.ValueExpression;
import mchorse.aperture.camera.values.ValueKeyframeChannel;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.math.IValue;
import mchorse.mclib.math.MathBuilder;
import mchorse.mclib.math.Variable;
import mchorse.mclib.utils.MathUtils;

public class RemapperModifier extends AbstractModifier
{
    public MathBuilder builder = new MathBuilder();

    public Variable ticks;
    public Variable offset;
    public Variable partial;
    public Variable duration;
    public Variable progress;
    public Variable factor;

    public final ValueBoolean keyframes = new ValueBoolean("keyframes");
    public final ValueKeyframeChannel channel = new ValueKeyframeChannel("channel");
    public final ValueExpression expression = new ValueExpression("expression", this.builder);

    public RemapperModifier()
    {
        super();

        this.register(this.keyframes);
        this.register(this.channel);
        this.register(this.expression);

        this.ticks = new Variable("t", 0);
        this.offset = new Variable("o", 0);
        this.partial = new Variable("pt", 0);
        this.duration = new Variable("d", 0);
        this.progress = new Variable("p", 0);
        this.factor = new Variable("f", 0);

        this.builder.register(this.ticks);
        this.builder.register(this.offset);
        this.builder.register(this.partial);
        this.builder.register(this.duration);
        this.builder.register(this.progress);
        this.builder.register(this.factor);

        this.channel.get().insert(0, 0);
        this.channel.get().insert(Aperture.duration.get(), 1);
    }

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        if (fixture == null)
        {
            return;
        }

        IValue value = this.expression.get();
        double factor = 0;

        if (this.keyframes.get())
        {
            factor = this.channel.get().interpolate(offset + previewPartialTick);
        }
        else if (value != null)
        {
            this.ticks.set(ticks);
            this.offset.set(offset);
            this.partial.set(previewPartialTick);
            this.duration.set(fixture.getDuration());
            this.progress.set(ticks + previewPartialTick);
            this.factor.set((double) (offset + previewPartialTick) / this.duration.get().doubleValue());

            factor = value.get().doubleValue();
        }

        factor *= fixture.getDuration();
        factor = MathUtils.clamp(factor, 0, fixture.getDuration());

        fixture.applyFixture((long) factor, (float) (factor % 1), profile, pos);
    }

    @Override
    public AbstractModifier create()
    {
        return new RemapperModifier();
    }

    @Override
    public void breakDown(AbstractModifier original, long offset, long duration)
    {
        super.breakDown(original, offset, duration);

        this.channel.get().moveX(-offset);
    }
}