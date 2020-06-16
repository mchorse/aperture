package mchorse.aperture.camera.fixtures;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.client.gui.panels.GuiManualFixturePanel;
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
	public float speed = 1;

	@Expose
	public int shift = 1;

	@Expose
	public List<List<RenderFrame>> frames = new ArrayList<List<RenderFrame>>();

	public List<RenderFrame> recorded = new ArrayList<RenderFrame>();

	public ManualFixture(long duration)
	{
		super(duration);
	}

	/**
	 * Get total duration of recorded data
	 */
	public int getEndTick()
	{
		return (int) (this.frames.size() / this.speed + this.shift);
	}

	@Override
	public void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
	{
		int size = this.frames.size();

		if (size <= 0)
		{
			return;
		}

		float tick = ticks + previewPartialTick;
		int index = (int) ((int) tick * this.speed) - this.shift;
		previewPartialTick = tick % 1;

		if (index < 0)
		{
			this.frames.get(0).get(0).apply(pos);

			return;
		}
		else if (index >= size)
		{
			List<RenderFrame> lastTick = this.frames.get(size - 1);

			lastTick.get(lastTick.size() - 1).apply(pos);

			return;
		}

		List<RenderFrame> lastTick = index - 1 >= 0 ? this.frames.get(index - 1) : null;
		RenderFrame last = lastTick == null || lastTick.isEmpty() ? null : lastTick.get(lastTick.size() - 1);
		float lastPt = last == null ? 0 : last.pt - 1;

		for (RenderFrame frame : this.frames.get(index))
		{
			if (frame.pt > previewPartialTick && previewPartialTick >= lastPt)
			{
				frame.apply(pos);

				return;
			}

			last = frame;
			lastPt = frame.pt;
		}

		if (last != null)
		{
			last.apply(pos);
		}
	}

	/**
	 * Reorganize the recorded data in a much efficient structure
	 */
	public void setupRecorded()
	{
		if (this.recorded.isEmpty())
		{
			return;
		}

		this.frames.clear();

		List<RenderFrame> tick = new ArrayList<RenderFrame>();
		RenderFrame last = this.recorded.get(0);
		int lastTick = last.tick;

		for (RenderFrame frame : this.recorded)
		{
			if (frame.tick > lastTick)
			{
				this.frames.add(tick);
				last.pt = 0;

				/* Fill missing ticks */
				while (lastTick + 1 < frame.tick)
				{
					tick = new ArrayList<RenderFrame>();
					tick.add(last.copy());
					lastTick += 1;

					this.frames.add(tick);
				}

				lastTick = frame.tick;
				tick = new ArrayList<RenderFrame>();
			}

			tick.add(frame);
			last = frame;
		}

		if (!tick.isEmpty())
		{
			this.frames.add(tick);
		}

		this.recorded.clear();
	}

	@Override
	public AbstractFixture copy()
	{
		ManualFixture fixture = new ManualFixture(this.duration);

		AbstractFixture.copyModifiers(this, fixture);
		fixture.name = this.name;
		fixture.shift = this.shift;
		fixture.speed = this.speed;

		for (List<RenderFrame> tick : this.frames)
		{
			List<RenderFrame> list = new ArrayList<RenderFrame>();

			for (RenderFrame frame : tick)
			{
				list.add(frame.copy());
			}

			fixture.frames.add(list);
		}

		return fixture;
	}

	/* Save/load methods */

	@Override
	public void fromJSON(JsonObject object)
	{
		super.fromJSON(object);

		if (this.frames == null)
		{
			this.frames = new ArrayList<List<RenderFrame>>();
		}
	}

	@Override
	public void fromByteBuf(ByteBuf buffer)
	{
		super.fromByteBuf(buffer);

		this.frames.clear();
		this.shift = buffer.readInt();
		this.speed = buffer.readFloat();

		for (int i = 0, c = buffer.readInt(); i < c; i ++)
		{
			List<RenderFrame> tick = new ArrayList<RenderFrame>();

			for (int j = 0, d = buffer.readInt(); j < d; j ++)
			{
				RenderFrame frame = new RenderFrame();

				frame.position(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
				frame.angle(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
				frame.pt = buffer.readFloat();

				tick.add(frame);
			}

			this.frames.add(tick);
		}
	}

	@Override
	public void toByteBuf(ByteBuf buffer)
	{
		super.toByteBuf(buffer);

		buffer.writeInt(this.shift);
		buffer.writeFloat(this.speed);
		buffer.writeInt(this.frames.size());

		for (List<RenderFrame> tick : this.frames)
		{
			buffer.writeInt(tick.size());

			for (RenderFrame frame : tick)
			{
				buffer.writeDouble(frame.x);
				buffer.writeDouble(frame.y);
				buffer.writeDouble(frame.z);
				buffer.writeFloat(frame.yaw);
				buffer.writeFloat(frame.pitch);
				buffer.writeFloat(frame.roll);
				buffer.writeFloat(frame.fov);
				buffer.writeFloat(frame.pt);
			}
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

		@Expose
		public float pt;

		/**
		 * Used only during recording
		 */
		public int tick;

		public RenderFrame()
		{}

		@SideOnly(Side.CLIENT)
		public RenderFrame(EntityPlayer player, float partialTicks)
		{
			this.fromPlayer(player, partialTicks);
		}

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
			this.pt = partialTicks;
			this.tick = GuiManualFixturePanel.tick;
		}

		public RenderFrame copy()
		{
			RenderFrame frame = new RenderFrame();

			frame.position(this.x, this.y, this.z);
			frame.angle(this.yaw, this.pitch, this.roll, this.fov);
			frame.pt = this.pt;

			return frame;
		}
	}
}