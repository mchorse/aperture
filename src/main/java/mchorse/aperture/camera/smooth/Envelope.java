package mchorse.aperture.camera.smooth;

import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.Interpolations;

public class Envelope
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

	public void copy(Envelope envelope)
	{
		this.enabled = envelope.enabled;
		this.relative = envelope.relative;
		this.startX = envelope.startX;
		this.startDuration = envelope.startDuration;
		this.endX = envelope.endX;
		this.endDuration = envelope.endDuration;
		this.interpolation = envelope.interpolation;
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
		float envelope = Interpolations.envelope(tick, this.startX, this.startX + this.startDuration, this.getEndDuration(duration), this.getEndX(duration));

		return this.interpolation.interpolate(0, 1, envelope);
	}

	public void toByteBuf(ByteBuf buffer)
	{
		buffer.writeBoolean(this.enabled);
		buffer.writeBoolean(this.relative);
		buffer.writeFloat(this.startX);
		buffer.writeFloat(this.startDuration);
		buffer.writeFloat(this.endX);
		buffer.writeFloat(this.endDuration);
		buffer.writeInt(this.interpolation.ordinal());
	}

	public void fromByteBuf(ByteBuf buffer)
	{
		this.enabled = buffer.readBoolean();
		this.relative = buffer.readBoolean();
		this.startX = buffer.readFloat();
		this.startDuration = buffer.readFloat();
		this.endX = buffer.readFloat();
		this.endDuration = buffer.readFloat();
		this.interpolation = Interpolation.values()[buffer.readInt()];
	}
}