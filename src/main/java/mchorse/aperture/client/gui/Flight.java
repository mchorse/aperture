package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class Flight
{
    private String stringSpeed = I18n.format("aperture.gui.editor.speed");

    public boolean enabled;
    public boolean vertical;
    public int speed = 1000;

    private int dragging;
    private int lastX;
    private int lastY;
    private long lastSpeed;

    private float getSpeedFactor(int direction)
    {
        float factor = 1000;
        boolean zoomIn = direction > 0;

        if ((zoomIn && this.speed <= 10) || (!zoomIn && this.speed < 10))
        {
            factor = 1;
        }
        else if ((zoomIn && this.speed <= 100) || (!zoomIn && this.speed < 100))
        {
            factor = 10;
        }
        else if ((zoomIn && this.speed <= 1000) || (!zoomIn && this.speed < 1000))
        {
            factor = 100;
        }

        return factor;
    }

    public void mouseScrolled(GuiContext context)
    {
        if (!this.enabled)
        {
            return;
        }

        this.speed -= Math.copySign(this.getSpeedFactor(context.mouseWheel), context.mouseWheel);
        this.speed = MathHelper.clamp(this.speed, 1, 50000);
    }

    public void animate(GuiContext context, Position position)
    {
        if (!this.enabled)
        {
            this.lastX = context.mouseX;
            this.lastY = context.mouseY;

            return;
        }

        this.dragging = -1;

        if (Mouse.isButtonDown(0))
        {
            this.dragging = 0;
        }
        else if (Mouse.isButtonDown(1))
        {
            this.dragging = 1;
        }
        else if (Mouse.isButtonDown(2))
        {
            this.dragging = 2;
        }

        float f = this.speed / 1000F;
        float multiplier = 1F;

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            multiplier *= 5;
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
        {
            multiplier *= 0.2F;
        }

        double factor = 0.1 * f * multiplier;
        double angleFactor = 0.35 * f * multiplier;

        float yaw = position.angle.yaw;
        float pitch = position.angle.pitch;
        float roll = position.angle.roll;
        float fov = position.angle.fov;

        if (this.dragging != -1)
        {
            if (this.dragging == 0)
            {
                yaw += (context.mouseX - this.lastX) * (multiplier * 0.35F);
                pitch += (context.mouseY - this.lastY) * (multiplier * 0.35F);
            }
            else if (this.dragging == 1)
            {
                roll += (context.mouseX - this.lastX) * (multiplier * 0.35F);
            }
            else if (this.dragging == 2)
            {
                fov += (context.mouseY - this.lastY) * (multiplier * 0.35F);
            }
        }

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

        int speedFactor = 0;
        int speedDelay = (int) (100 * 1 / multiplier);

        if (Keyboard.isKeyDown(Aperture.flightCameraSpeedPlus.get()) || Keyboard.isKeyDown(Aperture.flightCameraSpeedMinus.get()))
        {
            speedFactor = Keyboard.isKeyDown(Aperture.flightCameraSpeedPlus.get()) ? 1 : -1;
        }

        if (speedFactor != 0 && System.currentTimeMillis() > this.lastSpeed + speedDelay)
        {
            this.speed -= Math.copySign(this.getSpeedFactor(speedFactor), speedFactor);
            this.speed = MathHelper.clamp(this.speed, 1, 50000);
            this.lastSpeed = System.currentTimeMillis();
        }

        this.lastX = context.mouseX;
        this.lastY = context.mouseY;
    }

	public void drawSpeed(FontRenderer font, int x, int y)
    {
        float flightSpeed = this.speed / 1000F;
        String speedFormat = "%.0f";

        if (flightSpeed < 0.01F) speedFormat = "%.3f";
        else if (flightSpeed < 0.1F) speedFormat = "%.2f";
        else if (flightSpeed < 1F) speedFormat = "%.1f";

        String speed = String.format(this.stringSpeed + ": " + speedFormat, flightSpeed);
        int width = font.getStringWidth(speed);

        x -= width;

        Gui.drawRect(x - 2, y - 3, x + width + 2, y + 10, 0xbb000000);
        font.drawStringWithShadow(speed, x, y, 0xffffff);
	}
}