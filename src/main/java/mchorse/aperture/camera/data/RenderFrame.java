package mchorse.aperture.camera.data;

import com.google.gson.JsonObject;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.panels.GuiManualFixturePanel;
import mchorse.aperture.utils.OptifineHelper;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RenderFrame
{
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public float roll;
    public float fov;
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

        if (OptifineHelper.isZooming())
        {
            this.fov *= 0.25F;
        }
    }

    public RenderFrame copy()
    {
        RenderFrame frame = new RenderFrame();

        frame.position(this.x, this.y, this.z);
        frame.angle(this.yaw, this.pitch, this.roll, this.fov);
        frame.pt = this.pt;

        return frame;
    }

    public void fromJSON(JsonObject object)
    {
        this.x = object.get("x").getAsDouble();
        this.y = object.get("y").getAsDouble();
        this.z = object.get("z").getAsDouble();
        this.yaw = object.get("yaw").getAsFloat();
        this.pitch = object.get("pitch").getAsFloat();
        this.roll = object.get("roll").getAsFloat();
        this.fov = object.get("fov").getAsFloat();
        this.pt = object.get("pt").getAsFloat();
    }

    public JsonObject toJSON()
    {
        JsonObject object = new JsonObject();

        object.addProperty("x", this.x);
        object.addProperty("y", this.y);
        object.addProperty("z", this.z);
        object.addProperty("yaw", this.yaw);
        object.addProperty("pitch", this.pitch);
        object.addProperty("roll", this.roll);
        object.addProperty("fov", this.fov);
        object.addProperty("pt", this.pt);

        return object;
    }
}
