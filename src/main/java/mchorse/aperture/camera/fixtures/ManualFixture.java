package mchorse.aperture.camera.fixtures;

import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ManualFixture extends AbstractFixture
{
	@Expose
	public int framerate;

	@Expose
	public List<RenderFrame> list = new ArrayList<RenderFrame>();

	public ManualFixture(long duration)
	{
		super(duration);
	}

	@Override
	public void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
	{
		int size = this.list.size();

		if (size > 0)
		{
			int index = (int) ((ticks + previewPartialTick) / 20F * this.framerate);

			if (index >= size)
			{
				index = size - 1;
			}

			this.list.get(index).apply(pos);
		}
	}

	@Override
	public AbstractFixture copy()
	{
		ManualFixture fixture = new ManualFixture(this.duration);

		AbstractFixture.copyModifiers(this, fixture);
		fixture.name = this.name;
		fixture.framerate = this.framerate;

		for (RenderFrame frame : this.list)
		{
			fixture.list.add(frame.copy());
		}

		return fixture;
	}

	/* Save/load methods */

	@Override
	public void fromByteBuf(ByteBuf buffer)
	{
		super.fromByteBuf(buffer);

		this.framerate = buffer.readInt();
		this.list.clear();

		for (int i = 0, c = buffer.readInt(); i < c; i ++)
		{
			RenderFrame frame = new RenderFrame();

			frame.position(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
			frame.angle(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());

			this.list.add(frame);
		}
	}

	@Override
	public void toByteBuf(ByteBuf buffer)
	{
		super.toByteBuf(buffer);

		buffer.writeInt(this.framerate);
		buffer.writeInt(this.list.size());

		for (RenderFrame frame : this.list)
		{
			buffer.writeDouble(frame.x);
			buffer.writeDouble(frame.y);
			buffer.writeDouble(frame.z);
			buffer.writeFloat(frame.yaw);
			buffer.writeFloat(frame.pitch);
			buffer.writeFloat(frame.roll);
			buffer.writeFloat(frame.fov);
		}
	}

	public static class RenderFrame
	{
		@Expose
		public double x;

		@Expose
		public double y;

		@Expose
		public double z;

		@Expose
		public float yaw;

		@Expose
		public float pitch;

		@Expose
		public float roll;

		@Expose
		public float fov;

		public void position(double x, double y, double z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void angle(float yaw, float pitch, float roll, float fov)
		{
			this.yaw = yaw;
			this.pitch = pitch;
			this.roll = roll;
			this.fov = fov;
		}

		public void apply(Position pos)
		{
			pos.point.set(this.x, this.y, this.z);
			pos.angle.set(this.yaw, this.pitch, this.roll, this.fov);
		}

		@SideOnly(Side.CLIENT)
		public void fromPlayer(EntityPlayer player, float partialTicks)
		{
			this.x = Interpolations.lerp(player.prevPosX, player.posX, partialTicks);
			this.y = Interpolations.lerp(player.prevPosY, player.posY, partialTicks);
			this.z = Interpolations.lerp(player.prevPosZ, player.posZ, partialTicks);
			this.yaw = player.rotationYaw;
			this.pitch = player.rotationPitch;
			this.roll = ClientProxy.control.roll;
			this.fov = Minecraft.getMinecraft().gameSettings.fovSetting;
		}

		public RenderFrame copy()
		{
			RenderFrame frame = new RenderFrame();

			frame.position(this.x, this.y, this.z);
			frame.angle(this.yaw, this.pitch, this.roll, this.fov);

			return frame;
		}
	}
}