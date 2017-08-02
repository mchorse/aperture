package mchorse.aperture.client.gui;

import org.lwjgl.input.Keyboard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class Flight
{
    public boolean enabled = false;

    public void animate(EntityPlayer player)
    {
        float multiplier = 1;

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            multiplier = 5;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
        {
            multiplier = 0.2F;
        }

        double factor = 0.1 * multiplier;
        double angleFactor = 0.5 * multiplier;

        float yaw = player.rotationYaw;
        float pitch = player.rotationPitch;

        if (Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
        {
            pitch += (Keyboard.isKeyDown(Keyboard.KEY_UP) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
        {
            yaw += (Keyboard.isKeyDown(Keyboard.KEY_LEFT) ? -angleFactor : angleFactor);
        }

        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;

        double xx = 0;
        double yy = 0;
        double zz = 0;

        if (Keyboard.isKeyDown(Keyboard.KEY_Z) || Keyboard.isKeyDown(Keyboard.KEY_X))
        {
            yy = (Keyboard.isKeyDown(Keyboard.KEY_Z) ? factor : -factor);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D))
        {
            xx = (Keyboard.isKeyDown(Keyboard.KEY_A) ? factor : -factor);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S))
        {
            zz = (Keyboard.isKeyDown(Keyboard.KEY_W) ? factor : -factor);
        }

        if (xx != 0 || yy != 0 || zz != 0 || yaw != player.rotationYaw || pitch != player.rotationPitch)
        {
            Vec3d vec = new Vec3d(xx, yy, zz);

            vec = vec.rotateYaw(-yaw / 180 * (float) Math.PI);

            x += vec.xCoord;
            y += vec.yCoord;
            z += vec.zCoord;

            player.setPositionAndRotation(x, y, z, yaw, pitch);
            player.setVelocity(0, 0, 0);
        }
    }
}