package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.utils.APIcons;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.MathUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

@SideOnly(Side.CLIENT)
public class Flight implements IGuiElement
{
    private IKey stringSpeed = IKey.lang("aperture.gui.editor.speed");
    private IKey stringDistance = IKey.lang("aperture.gui.editor.distance");

    private GuiCameraEditor editor;

    private boolean enabled;
    private MovementType type = MovementType.HORIZONTAL;
    private int speed = 1000;

    private int dragging;
    private int lastX;
    private int lastY;
    private long lastSpeed;

    private Vector3d lastPosition = new Vector3d();
    private float distance;
    private boolean update;

    public Flight(GuiCameraEditor editor)
    {
        this.editor = editor;
    }

    public boolean isFlightEnabled()
    {
        return this.enabled;
    }

    public void setFlightEnabled(boolean enabled)
    {
        this.enabled = enabled;
        this.calculateOrigin();
    }

    public void toggleMovementType()
    {
        MovementType[] values = MovementType.values();
        int direction = GuiScreen.isAltKeyDown() ? -1 : 1;

        this.setMovementType(values[MathUtils.cycler(this.type.ordinal() + direction, 0, values.length - 1)]);
    }

    public void setMovementType(MovementType type)
    {
        this.type = type;
        this.calculateOrigin();
    }

    public MovementType getMovementType()
    {
        return this.type;
    }

    private void calculateOrigin()
    {
        if (this.type != MovementType.ORBIT)
        {
            return;
        }

        Position position = this.editor.position;
        Vec3d vec = new Vec3d(0, 0, this.distance);
        double x = position.point.x;
        double y = position.point.y;
        double z = position.point.z;

        vec = vec.rotatePitch(-position.angle.pitch / 180 * (float) Math.PI);
        vec = vec.rotateYaw(-position.angle.yaw / 180 * (float) Math.PI);

        x += vec.x;
        y += vec.y;
        z += vec.z;

        this.lastPosition.set(x, y, z);
    }

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

    @Override
    public boolean mouseClicked(GuiContext context)
    {
        this.dragging = context.mouseButton;

        return false;
    }

    @Override
    public boolean mouseScrolled(GuiContext context)
    {
        if (!this.enabled)
        {
            return false;
        }

        if (GuiScreen.isAltKeyDown())
        {
            this.distance += Math.copySign(this.getZoomFactor(), context.mouseWheel);
            this.distance = MathUtils.clamp(this.distance, 0, 100);
            this.update = true;
        }
        else
        {
            this.speed -= Math.copySign(this.getSpeedFactor(context.mouseWheel), context.mouseWheel);
            this.speed = MathHelper.clamp(this.speed, 1, 50000);
        }

        return false;
    }

    protected float getZoomFactor()
    {
        if (this.distance < 1) return 0.05F;
        if (this.distance > 30) return 5F;
        if (this.distance > 10) return 1F;
        if (this.distance > 3) return 0.5F;

        return 0.1F;
    }

    @Override
    public void mouseReleased(GuiContext context)
    {
        this.dragging = -1;
    }

    public void animate(GuiContext context, Position position)
    {
        if (!this.enabled)
        {
            this.lastX = context.mouseX;
            this.lastY = context.mouseY;

            return;
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
        boolean gotDragged = false;

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

            gotDragged = true;
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

        if (gotDragged && this.type == MovementType.ORBIT || this.update)
        {
            Vector3d vec = new Vector3d(0, 0, this.distance);

            Matrix3d mat = new Matrix3d();
            mat.rotX((-pitch) / 180 * (float) Math.PI);
            mat.transform(vec);
            mat.rotY((180 - yaw) / 180 * (float) Math.PI);
            mat.transform(vec);

            position.point.set(this.lastPosition.x + vec.x, this.lastPosition.y + vec.y, this.lastPosition.z + vec.z);
            position.angle.set(yaw, pitch, roll, fov);

            this.update = false;
        }
        else if (xx != 0 || yy != 0 || zz != 0 || yaw != position.angle.yaw || pitch != position.angle.pitch || roll != position.angle.roll || fov != position.angle.fov)
        {
            Vec3d vec = new Vec3d(xx, yy, zz);

            if (this.type == MovementType.VERTICAL || this.type == MovementType.ORBIT)
            {
                vec = vec.rotatePitch(-pitch / 180 * (float) Math.PI);
            }

            vec = vec.rotateYaw(-yaw / 180 * (float) Math.PI);

            x += vec.x;
            y += vec.y;
            z += vec.z;

            position.point.set(x, y, z);
            position.angle.set(yaw, pitch, roll, fov);

            this.calculateOrigin();
        }

        int speedFactor = 0;
        int speedDelay = (int) (100 / multiplier);

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

        String speed = String.format(this.stringSpeed.get() + ": " + speedFormat, flightSpeed);
        int width = font.getStringWidth(speed);

        if (this.type == MovementType.ORBIT)
        {
            y -= 14;
        }

        Gui.drawRect(x - width - 2, y - 3, x + 2, y + 10, 0xbb000000);
        font.drawStringWithShadow(speed, x - width, y, 0xffffff);

        if (this.type == MovementType.ORBIT)
        {
            y += 14;

            speed = this.stringDistance.get() + ": " + this.distance;
            width = font.getStringWidth(speed);

            Gui.drawRect(x - width - 2, y - 3, x + 2, y + 10, 0xbb000000);
            font.drawStringWithShadow(speed, x - width, y, 0xffffff);
        }
	}

	/* Unimplemented GUI element methods */

    @Override
    public void resize()
    {}

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public boolean keyTyped(GuiContext guiContext)
    {
        return false;
    }

    @Override
    public void draw(GuiContext guiContext)
    {}

    public static enum MovementType
    {
        HORIZONTAL(APIcons.PLANE), VERTICAL(APIcons.HELICOPTER), ORBIT(APIcons.ORBIT);

        public final Icon icon;

        MovementType(Icon icon)
        {
            this.icon = icon;
        }
    }
}