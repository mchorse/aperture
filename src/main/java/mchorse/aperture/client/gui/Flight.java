package mchorse.aperture.client.gui;

import org.lwjgl.input.Keyboard;

import mchorse.aperture.camera.data.Position;
import net.minecraft.util.math.Vec3d;

public class Flight
{
    public boolean enabled = false;
    public float speed = 1;

    public void animate(Position position)
    {
        float multiplier = this.speed;

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            multiplier *= 5;
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
        {
            multiplier *= 0.2F;
        }

        double factor = 0.1 * multiplier;
        double angleFactor = 0.5 * multiplier;

        float yaw = position.angle.yaw;
        float pitch = position.angle.pitch;
        float roll = position.angle.roll;
        float fov = position.angle.fov;

        if (Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
        {
            pitch += (Keyboard.isKeyDown(Keyboard.KEY_UP) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
        {
            yaw += (Keyboard.isKeyDown(Keyboard.KEY_LEFT) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LBRACKET) || Keyboard.isKeyDown(Keyboard.KEY_RBRACKET))
        {
            roll += (Keyboard.isKeyDown(Keyboard.KEY_LBRACKET) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_APOSTROPHE) || Keyboard.isKeyDown(Keyboard.KEY_BACKSLASH))
        {
            fov += (Keyboard.isKeyDown(Keyboard.KEY_APOSTROPHE) ? -angleFactor : angleFactor);
        }

        double x = position.point.x;
        double y = position.point.y;
        double z = position.point.z;

        double xx = 0;
        double yy = 0;
        double zz = 0;

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            yy = (Keyboard.isKeyDown(Keyboard.KEY_SPACE) ? factor : -factor);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D))
        {
            xx = (Keyboard.isKeyDown(Keyboard.KEY_A) ? factor : -factor);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S))
        {
            zz = (Keyboard.isKeyDown(Keyboard.KEY_W) ? factor : -factor);
        }

        if (xx != 0 || yy != 0 || zz != 0 || yaw != position.angle.yaw || pitch != position.angle.pitch || roll != position.angle.roll || fov != position.angle.fov)
        {
            Vec3d vec = new Vec3d(xx, yy, zz);

            vec = vec.rotateYaw(-yaw / 180 * (float) Math.PI);

            x += vec.xCoord;
            y += vec.yCoord;
            z += vec.zCoord;

            position.point.set(x, y, z);
            position.angle.set(yaw, pitch, roll, fov);
        }
    }
}