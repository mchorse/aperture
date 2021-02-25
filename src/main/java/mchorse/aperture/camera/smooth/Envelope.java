package mchorse.aperture.camera.smooth;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.data.StructureBase;
import mchorse.aperture.camera.values.ValueInterpolation;
import mchorse.aperture.camera.values.ValueKeyframeChannel;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueFloat;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MathUtils;
import mchorse.mclib.utils.keyframes.KeyframeChannel;

public class Envelope extends StructureBase
{
    public final ValueBoolean enabled = new ValueBoolean("enabled");
    public final ValueBoolean relative = new ValueBoolean("relative", true);

    public final ValueFloat startX = new ValueFloat("startX");
    public final ValueFloat startDuration = new ValueFloat("startDuration", 10);
    public final ValueFloat endX = new ValueFloat("endX");
    public final ValueFloat endDuration = new ValueFloat("endDuration", 10);

    public final ValueInterpolation interpolation = new ValueInterpolation("interpolation");

    public final ValueBoolean keyframes = new ValueBoolean("keyframes");
    public final ValueKeyframeChannel channel = new ValueKeyframeChannel("channel");

    public Envelope()
    {
        this.register(this.enabled);
        this.register(this.relative);
        this.register(this.startX);
        this.register(this.startDuration);
        this.register(this.endX);
        this.register(this.endDuration);
        this.register(this.interpolation);
        this.register(this.keyframes);
        this.register(this.channel);

        this.channel.get().insert(0, 0);
        this.channel.get().insert(Aperture.duration.get(), 1);
    }

    public Envelope copy()
    {
        Envelope envelope = new Envelope();

        envelope.copy(this);

        return envelope;
    }

    public float getStartX(long duration)
    {
        return this.startX.get();
    }

    public float getStartDuration(long duration)
    {
        return this.startX.get() + this.startDuration.get();
    }

    public float getEndX(long duration)
    {
        return this.relative.get() ? duration - this.endX.get() : this.endX.get();
    }

    public float getEndDuration(long duration)
    {
        return this.relative.get() ? duration - this.endX.get() - this.endDuration.get() : this.endX.get() - this.endDuration.get();
    }

    public float factorEnabled(long duration, float tick)
    {
        if (!this.enabled.get())
        {
            return 1;
        }

        return this.factor(duration, tick);
    }

    public float factor(long duration, float tick)
    {
        float envelope = 0;

        if (this.keyframes.get())
        {
            if (!this.channel.get().isEmpty())
            {
                envelope = MathUtils.clamp((float) this.channel.get().interpolate(tick), 0, 1);
            }
        }
        else
        {
            float startX = this.startX.get();

            envelope = Interpolations.envelope(tick, startX, startX + this.startDuration.get(), this.getEndDuration(duration), this.getEndX(duration));
            envelope = this.interpolation.get().interpolate(0, 1, envelope);
        }

        return envelope;
    }
}