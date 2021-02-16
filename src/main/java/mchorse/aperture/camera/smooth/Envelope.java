package mchorse.aperture.camera.smooth;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.Aperture;
import mchorse.mclib.network.IByteBufSerializable;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MathUtils;
import mchorse.mclib.utils.keyframes.KeyframeChannel;

public class Envelope implements IByteBufSerializable
{
    @Expose
    public boolean enabled;

    @Expose
    public boolean relative = true;

    @Expose
    public float startX;

    @Expose
    public float startDuration = 10;

    @Expose
    public float endX;

    @Expose
    public float endDuration = 10;

    @Expose
    public Interpolation interpolation = Interpolation.LINEAR;

    @Expose
    public boolean keyframes;

    @Expose
    public KeyframeChannel channel = this.create();

    public KeyframeChannel create()
    {
        KeyframeChannel channel = new KeyframeChannel();

        channel.insert(0, 0);
        channel.insert(Aperture.duration.get(), 1);

        return channel;
    }

    public void copy(Envelope envelope)
    {
        this.enabled = envelope.enabled;
        this.relative = envelope.relative;
        this.startX = envelope.startX;
        this.startDuration = envelope.startDuration;
        this.endX = envelope.endX;
        this.endDuration = envelope.endDuration;
        this.interpolation = envelope.interpolation;
        this.keyframes = envelope.keyframes;
        this.channel.copy(envelope.channel);
    }

    public float getStartX(long duration)
    {
        return this.startX;
    }

    public float getStartDuration(long duration)
    {
        return this.startX + this.startDuration;
    }

    public float getEndX(long duration)
    {
        return this.relative ? duration - this.endX : this.endX;
    }

    public float getEndDuration(long duration)
    {
        return this.relative ? duration - this.endX - this.endDuration : this.endX - this.endDuration;
    }

    public float factorEnabled(long duration, float tick)
    {
        if (!this.enabled)
        {
            return 1;
        }

        return this.factor(duration, tick);
    }

    public float factor(long duration, float tick)
    {
        float envelope = 0;

        if (this.keyframes && this.channel != null)
        {
            if (!this.channel.isEmpty())
            {
                envelope = MathUtils.clamp((float) this.channel.interpolate(tick), 0, 1);
            }
        }
        else
        {
            envelope = Interpolations.envelope(tick, this.startX, this.startX + this.startDuration, this.getEndDuration(duration), this.getEndX(duration));
            envelope = this.interpolation.interpolate(0, 1, envelope);
        }

        return envelope;
    }

    public void toJSON(JsonObject object)
    {}

    public void fromJSON(JsonObject object)
    {
        if (this.channel == null)
        {
            this.channel = this.create();
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeBoolean(this.enabled);
        buffer.writeBoolean(this.relative);
        buffer.writeFloat(this.startX);
        buffer.writeFloat(this.startDuration);
        buffer.writeFloat(this.endX);
        buffer.writeFloat(this.endDuration);
        buffer.writeInt(this.interpolation.ordinal());
        buffer.writeBoolean(this.keyframes);
        this.channel.toBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.enabled = buffer.readBoolean();
        this.relative = buffer.readBoolean();
        this.startX = buffer.readFloat();
        this.startDuration = buffer.readFloat();
        this.endX = buffer.readFloat();
        this.endDuration = buffer.readFloat();
        this.interpolation = Interpolation.values()[buffer.readInt()];
        this.keyframes = buffer.readBoolean();
        this.channel.fromBytes(buffer);
    }
}