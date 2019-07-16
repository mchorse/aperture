package mchorse.aperture.camera.data;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

/**
 * Angle class
 * 
 * Represents a camera angle: yaw, pitch and roll, and also Field of 
 * View angle.
 */
public class Angle
{
    @Expose
    public float yaw;

    @Expose
    public float pitch;

    @Expose
    public float roll;

    @Expose
    public float fov = 70.0F;

    public static Angle fromByteBuf(ByteBuf buffer)
    {
        return new Angle(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public Angle(float yaw, float pitch, float roll, float fov)
    {
        this.set(yaw, pitch, roll, fov);
    }

    public Angle(float yaw, float pitch)
    {
        this.set(yaw, pitch);
    }

    public void set(Angle angle)
    {
        this.set(angle.yaw, angle.pitch, angle.roll, angle.fov);
    }

    public void set(float yaw, float pitch, float roll, float fov)
    {
        this.set(yaw, pitch);
        this.roll = roll;
        this.fov = fov;
    }

    public void set(float yaw, float pitch)
    {
        if (Aperture.proxy.config.camera_smooth_clamp)
        {
            /* Clamp pitch */
            pitch = MathHelper.clamp(pitch, -90, 90);
        }

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void set(EntityPlayer player)
    {
        float fov = Minecraft.getMinecraft().gameSettings.fovSetting;

        this.set(player.rotationYaw, player.rotationPitch, ClientProxy.control.roll, fov);
    }

    public void toByteBuf(ByteBuf buffer)
    {
        buffer.writeFloat(this.yaw);
        buffer.writeFloat(this.pitch);
        buffer.writeFloat(this.roll);
        buffer.writeFloat(this.fov);
    }

    @Override
    public Angle clone()
    {
        return new Angle(this.yaw, this.pitch, this.roll, this.fov);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).addValue(this.yaw).addValue(this.pitch).addValue(this.roll).addValue(this.fov).toString();
    }
}