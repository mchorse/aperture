package mchorse.aperture.client.gui;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.CameraRenderer.Color;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.FollowFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.DurablePosition;
import mchorse.aperture.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Fixtures popup
 * 
 * Allows to select the type of camera fixture the user wants to create.
 */
public class GuiFixturesPopup
{
    public Area area = new Area();
    public boolean visible;

    public IFixtureSelector selector;
    public List<GuiButton> buttons = new ArrayList<GuiButton>();

    public GuiButton idle;
    public GuiButton path;
    public GuiButton look;
    public GuiButton follow;
    public GuiButton circular;

    private Minecraft mc;

    public GuiFixturesPopup(IFixtureSelector selector)
    {
        this.selector = selector;
        this.mc = Minecraft.getMinecraft();

        int idleColor = 0xff000000 + Color.IDLE.hex;
        int pathColor = 0xff000000 + Color.PATH.hex;
        int lookColor = 0xff000000 + Color.LOOK.hex;
        int followColor = 0xff000000 + Color.FOLLOW.hex;
        int circularColor = 0xff000000 + Color.CIRCULAR.hex;

        this.idle = new GuiFlatButton(0, 0, 0, 0, 0, idleColor, idleColor - 0x00111111, I18n.format("aperture.gui.fixtures.idle"));
        this.path = new GuiFlatButton(0, 0, 0, 0, 0, pathColor, pathColor - 0x00111111, I18n.format("aperture.gui.fixtures.path"));
        this.look = new GuiFlatButton(0, 0, 0, 0, 0, lookColor, lookColor - 0x00111111, I18n.format("aperture.gui.fixtures.look"));
        this.follow = new GuiFlatButton(0, 0, 0, 0, 0, followColor, followColor - 0x00111111, I18n.format("aperture.gui.fixtures.follow"));
        this.circular = new GuiFlatButton(0, 0, 0, 0, 0, circularColor, circularColor - 0x00111111, I18n.format("aperture.gui.fixtures.circular"));

        this.buttons.add(this.idle);
        this.buttons.add(this.path);
        this.buttons.add(this.look);
        this.buttons.add(this.follow);
        this.buttons.add(this.circular);
    }

    public void update(int x, int y, int w, int h)
    {
        this.area.set(x, y, w, h);

        int bh = (h - 2) / 5;
        int bw = (w - 2);

        this.idle.width = this.path.width = this.look.width = this.follow.width = this.circular.width = bw;
        this.idle.height = this.path.height = this.look.height = this.follow.height = this.circular.height = bh;
        this.idle.xPosition = this.path.xPosition = this.look.xPosition = this.follow.xPosition = this.circular.xPosition = x + 1;

        this.idle.yPosition = y + 1;
        this.path.yPosition = y + 1 + bh;
        this.look.yPosition = y + 1 + bh * 2;
        this.follow.yPosition = y + 1 + bh * 3;
        this.circular.yPosition = y + 1 + bh * 4;
    }

    public boolean isInside(int x, int y)
    {
        return this.visible && this.area.isInside(x, y);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (!this.visible)
        {
            return;
        }

        if (mouseButton == 0)
        {
            for (int i = 0; i < this.buttons.size(); ++i)
            {
                GuiButton button = this.buttons.get(i);

                if (button.mousePressed(this.mc, mouseX, mouseY))
                {
                    button.playPressSound(this.mc.getSoundHandler());

                    this.actionPerformed(button);
                }
            }
        }

        this.visible = false;
    }

    /**
     * Select a fixture
     */
    private void actionPerformed(GuiButton button)
    {
        AbstractFixture fixture = null;
        EntityPlayer player = this.mc.thePlayer;
        long duration = Aperture.proxy.config.camera_duration;

        if (button == this.idle)
        {
            IdleFixture idle = new IdleFixture(duration);

            idle.position.set(player);
            fixture = idle;
        }
        else if (button == this.path)
        {
            PathFixture path = new PathFixture(duration);

            path.addPoint(new DurablePosition(player));
            fixture = path;
        }
        else if (button == this.look)
        {
            LookFixture look = new LookFixture(duration);
            look.position.set(player);

            fixture = look;
        }
        else if (button == this.follow)
        {
            FollowFixture follow = new FollowFixture(duration);

            follow.position.angle.set(player);
            fixture = follow;
        }
        else if (button == this.circular)
        {
            CircularFixture circular = new CircularFixture(duration);

            circular.start.set(player);
            circular.pitch = player.rotationPitch;
            fixture = circular;
        }

        if (fixture != null && this.selector != null)
        {
            this.selector.createFixture(fixture);
        }
    }

    /**
     * Draw buttons on the screen
     */
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0xaa000000);

            for (GuiButton button : this.buttons)
            {
                button.drawButton(this.mc, mouseX, mouseY);
            }
        }
    }

    /**
     * Solid color button
     */
    public static class GuiFlatButton extends GuiButton
    {
        public int color;
        public int activeColor;

        public GuiFlatButton(int buttonId, int x, int y, int widthIn, int heightIn, int color, int activeColor, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);

            this.color = color;
            this.activeColor = activeColor;
        }

        public GuiButton setColors(int color, int active)
        {
            this.color = color;
            this.activeColor = active;

            return this;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY)
        {
            if (this.visible)
            {
                FontRenderer fontrenderer = mc.fontRendererObj;
                this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

                int color = this.hovered ? this.activeColor : this.color;

                Gui.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, color);
                this.mouseDragged(mc, mouseX, mouseY);
                int j = 14737632;

                if (this.packedFGColour != 0)
                {
                    j = this.packedFGColour;
                }
                else if (!this.enabled)
                {
                    j = 10526880;
                }
                else if (this.hovered)
                {
                    j = 16777120;
                }

                this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
            }
        }
    }

    public static interface IFixtureSelector
    {
        public void createFixture(AbstractFixture fixture);
    }
}