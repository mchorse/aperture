package mchorse.aperture.camera.fixtures;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.utils.Interpolation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DollyFixture extends IdleFixture
{
	@Expose
	public float distance = 1F;

	@Expose
	public Interpolation interp = Interpolation.LINEAR;

	@Expose
	public float yaw;

	@Expose
	public float pitch;

	public DollyFixture(long duration)
	{
		super(duration);
	}

	@Override
	public void fromPlayer(EntityPlayer player)
	{
		super.fromPlayer(player);

		this.yaw = this.position.angle.yaw;
		this.pitch = this.position.angle.pitch;
	}

	@Override
	public void applyFixture(long ticks, float partialTicks, float previewPartialTick, CameraProfile profile, Position pos)
	{
		pos.copy(this.position);

		Interpolation interp = this.interp == null ? Interpolation.LINEAR : this.interp;
		double x = this.position.point.x;
		double y = this.position.point.y;
		double z = this.position.point.z;

		final float degToPi = (float) Math.PI / 180;

		float cos = MathHelper.cos(-this.yaw * degToPi - (float) Math.PI);
		float sin = MathHelper.sin(-this.yaw * degToPi - (float) Math.PI);
		float cos2 = -MathHelper.cos(-this.pitch * degToPi);
		float sin2 = MathHelper.sin(-this.pitch * degToPi);
		Vec3d look = new Vec3d(sin * cos2, sin2, cos * cos2).normalize().scale(this.distance);

		partialTicks = (ticks + previewPartialTick) / this.getDuration();

		x = interp.interpolate(x, x + look.x, partialTicks);
		y = interp.interpolate(y, y + look.y, partialTicks);
		z = interp.interpolate(z, z + look.z, partialTicks);

		pos.point.set(x, y, z);
	}

	@Override
	public AbstractFixture create(long duration)
	{
		return new DollyFixture(duration);
	}

	@Override
	public void copy(AbstractFixture from)
	{
		super.copy(from);

		if (from instanceof DollyFixture)
		{
			DollyFixture dolly = (DollyFixture) from;

			this.yaw = dolly.yaw;
			this.pitch = dolly.pitch;
			this.distance = dolly.distance;
			this.interp = dolly.interp;
		}
	}

	/* Save/load methods */

	@Override
	public void fromJSON(JsonObject object)
	{
		super.fromJSON(object);

		if (this.interp == null)
		{
			this.interp = Interpolation.LINEAR;
		}
	}

	@Override
	public void fromByteBuf(ByteBuf buffer)
	{
		super.fromByteBuf(buffer);

		this.yaw = buffer.readFloat();
		this.pitch = buffer.readFloat();
		this.distance = buffer.readFloat();

		int index = buffer.readInt();

		if (index >= 0 && index < Interpolation.values().length)
		{
			this.interp = Interpolation.values()[index];
		}
		else
		{
			this.interp = Interpolation.LINEAR;
		}
	}

	@Override
	public void toByteBuf(ByteBuf buffer)
	{
		super.toByteBuf(buffer);

		buffer.writeFloat(this.yaw);
		buffer.writeFloat(this.pitch);
		buffer.writeFloat(this.distance);
		buffer.writeInt(this.interp == null ? -1 : this.interp.ordinal());
	}
}