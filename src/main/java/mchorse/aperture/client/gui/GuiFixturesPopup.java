package mchorse.aperture.client.gui;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.FixtureRegistry.FixtureInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

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

    private Minecraft mc;

    public GuiFixturesPopup(IFixtureSelector selector)
    {
        this.selector = selector;
        this.mc = Minecraft.getMinecraft();

        for (FixtureInfo info : FixtureRegistry.CLIENT.values())
        {
            int color = 0xff000000 + info.color.getHex();

            this.buttons.add(new GuiFlatButton(info.type, 0, 0, 0, 0, color, color - 0x00111111, info.title));
        }
    }

    public void update(int x, int y, int w, int h)
    {
        this.area.set(x, y, w, h);

        int bh = (h - 2) / 6;
        int bw = (w - 2);
        int i = 0;

        for (GuiButton button : this.buttons)
        {
            GuiUtils.setSize(button, x + 1, y + 1 + bh * i, bw, bh);

            i++;
        }
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
        long duration = Aperture.proxy.config.camera_duration;
        AbstractFixture fixture = null;

        try
        {
            fixture = FixtureRegistry.fromType((byte) button.id, duration);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (fixture != null && this.selector != null)
        {
            fixture.fromPlayer(this.mc.player);
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