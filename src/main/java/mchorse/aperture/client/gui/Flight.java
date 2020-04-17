package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import org.lwjgl.input.Keyboard;

import mchorse.aperture.camera.data.Position;
import net.minecraft.util.math.Vec3d;

public class Flight
{
    public boolean enabled = false;
    public boolean vertical = false;
    public int speed = 1000;

    public void animate(Position position)
    {
        float multiplier = this.speed / 1000F;

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            multiplier *= 5;
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
        {
            multiplier *= 0.2F;
        }

        double factor = 0.1 * multiplier;
        double angleFactor = 0.35 * multiplier;

        float yaw = position.angle.yaw;
        float pitch = position.angle.pitch;
        float roll = position.angle.roll;
        float fov = position.angle.fov;

        if (Keyboard.isKeyDown(Aperture.flightCameraUp.get()) || Keyboard.isKeyDown(Aperture.flightCameraDown.get()))
        {
            pitch += (Keyboard.isKeyDown(Aperture.flightCameraUp.get()) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Aperture.flightCameraLeft.get()) || Keyboard.isKeyDown(Aperture.flightCameraRight.get()))
        {
            yaw += (Keyboard.isKeyDown(Aperture.flightCameraLeft.get()) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Aperture.flightCameraRollMinus.get()) || Keyboard.isKeyDown(Aperture.flightCameraRollPlus.get()))
        {
            roll += (Keyboard.isKeyDown(Aperture.flightCameraRollMinus.get()) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Aperture.flightCameraFovMinus.get()) || Keyboard.isKeyDown(Aperture.flightCameraFovPlus.get()))
        {
            fov += (Keyboard.isKeyDown(Aperture.flightCameraFovMinus.get()) ? -angleFactor : angleFactor);
        }

        double x = position.point.x;
        double y = position.point.y;
        double z = position.point.z;

        double xx = 0;
        double yy = 0;
        double zz = 0;

        if (Keyboard.isKeyDown(Aperture.flightUp.get()) || Keyboard.isKeyDown(Aperture.flightDown.get()))
        {
            yy = (Keyboard.isKeyDown(Aperture.flightUp.get()) ? factor : -factor);
        }

        if (Keyboard.isKeyDown(Aperture.flightLeft.get()) || Keyboard.isKeyDown(Aperture.flightRight.get()))
        {
            xx = (Keyboard.isKeyDown(Aperture.flightLeft.get()) ? factor : -factor);
        }

        if (Keyboard.isKeyDown(Aperture.flightForward.get()) || Keyboard.isKeyDown(Aperture.flightBackward.get()))
        {
            zz = (Keyboard.isKeyDown(Aperture.flightForward.get()) ? factor : -factor);
        }

        if (xx != 0 || yy != 0 || zz != 0 || yaw != position.angle.yaw || pitch != position.angle.pitch || roll != position.angle.roll || fov != position.angle.fov)
        {
            Vec3d vec = new Vec3d(xx, yy, zz);

            if (this.vertical)
            {
                vec = vec.rotatePitch(-pitch / 180 * (float) Math.PI);
            }

            vec = vec.rotateYaw(-yaw / 180 * (float) Math.PI);

            x += vec.x;
            y += vec.y;
            z += vec.z;

            position.point.set(x, y, z);
            position.angle.set(yaw, pitch, roll, fov);
        }
    }
}