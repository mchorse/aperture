package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.math.IValue;
import mchorse.mclib.math.MathBuilder;
import mchorse.mclib.math.Variable;
import mchorse.mclib.utils.MathUtils;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class RemapperModifier extends AbstractModifier
{
	@Expose
	public boolean keyframes;

	@Expose
	public KeyframeChannel channel;

	public MathBuilder builder = new MathBuilder();
	public IValue expression;

	public Variable ticks;
	public Variable offset;
	public Variable partial;
	public Variable duration;
	public Variable progress;
	public Variable value;

	public RemapperModifier()
	{
		this.ticks = new Variable("t", 0);
		this.offset = new Variable("o", 0);
		this.partial = new Variable("pt", 0);
		this.duration = new Variable("d", 0);
		this.progress = new Variable("p", 0);
		this.value = new Variable("value", 0);

		this.builder.register(this.ticks);
		this.builder.register(this.offset);
		this.builder.register(this.partial);
		this.builder.register(this.duration);
		this.builder.register(this.progress);
		this.builder.register(this.value);

		this.channel = new KeyframeChannel();
		this.channel.insert(0, 0);
	}

	@Override
	public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
	{
		if (fixture == null)
		{
			return;
		}

		double factor = 0;

		if (this.keyframes && this.channel != null)
		{
			factor = this.channel.interpolate(offset + previewPartialTick);
		}
		else if (!this.keyframes && this.expression != null)
		{
			this.ticks.set(ticks);
			this.offset.set(offset);
			this.partial.set(previewPartialTick);
			this.duration.set(fixture.getDuration());
			this.progress.set(ticks + previewPartialTick);

			factor = this.expression.get();
		}

		factor *= fixture.getDuration();
		factor = MathUtils.clamp(factor, 0, fixture.getDuration());

		fixture.applyFixture((long) factor, (float) (factor % 1), profile, pos);
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
	public AbstractModifier create()
	{
		return new RemapperModifier();
	}

	@Override
	public void copy(AbstractModifier from)
	{
		super.copy(from);

		if (from instanceof RemapperModifier)
		{
			RemapperModifier modifier = (RemapperModifier) from;

			this.keyframes = modifier.keyframes;
			this.channel.copy(modifier.channel);
			this.rebuildExpression(modifier.expression.toString());
		}
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
		super.fromJSON(object);

		if (object.has("expression"))
		{
			this.rebuildExpression(object.get("expression").getAsString());
		}

		if (this.channel == null)
		{
			this.channel = new KeyframeChannel();
			this.channel.insert(0, 0);
		}
	}

	@Override
	public void toByteBuf(ByteBuf buffer)
	{
		super.toByteBuf(buffer);

		buffer.writeBoolean(this.keyframes);
		this.channel.toByteBuf(buffer);
		ByteBufUtils.writeUTF8String(buffer, this.expression == null ? "" : this.expression.toString());
	}

	@Override
	public void fromByteBuf(ByteBuf buffer)
	{
		super.fromByteBuf(buffer);

		this.keyframes = buffer.readBoolean();
		this.channel.fromByteBuf(buffer);
		this.rebuildExpression(ByteBufUtils.readUTF8String(buffer));
	}
}