package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.utils.Interpolations;

/**
 * Drag modifier
 * 
 * This modifier is responsible for creating follow like 
 * behavior by memorizing previous position/angle and then 
 * linearly interpolating it using given factor.
 */
public class DragModifier extends ComponentModifier
{
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private float roll;
    private float fov;

    @Expose
    public float factor = 0.5F;

    public DragModifier()
    {}

    @Override
    public void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        if (offset == 0)
        {
            this.x = pos.point.x;
            this.y = pos.point.y;
            this.z = pos.point.z;
            this.yaw = pos.angle.yaw;
            this.pitch = pos.angle.pitch;
            this.roll = pos.angle.roll;
            this.fov = pos.angle.fov;
        }

        if (this.isActive(0)) pos.point.x = this.x = Interpolations.lerp(this.x, pos.point.x, this.factor);
        if (this.isActive(1)) pos.point.y = this.y = Interpolations.lerp(this.y, pos.point.y, this.factor);
        if (this.isActive(2)) pos.point.z = this.z = Interpolations.lerp(this.z, pos.point.z, this.factor);
        if (this.isActive(3)) pos.angle.yaw = this.yaw = Interpolations.lerpYaw(this.yaw, pos.angle.yaw, this.factor);
        if (this.isActive(4)) pos.angle.pitch = this.pitch = Interpolations.lerp(this.pitch, pos.angle.pitch, this.factor);
        if (this.isActive(5)) pos.angle.roll = this.roll = Interpolations.lerp(this.roll, pos.angle.roll, this.factor);
        if (this.isActive(6)) pos.angle.fov = this.fov = Interpolations.lerp(this.fov, pos.angle.fov, this.factor);
    }

    @Override
    public AbstractModifier create()
    {
        return new DragModifier();
    }

    @Override
    public void copy(AbstractModifier from)
    {
        super.copy(from);

        if (from instanceof DragModifier)
        {
            this.factor = ((DragModifier) from).factor;
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.factor = buffer.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeFloat(this.factor);
    }
}