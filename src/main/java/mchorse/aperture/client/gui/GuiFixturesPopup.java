package mchorse.aperture.client.gui;

import java.util.function.Consumer;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.FixtureRegistry.FixtureInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

/**
 * Fixtures popup
 * 
 * Allows to select the type of camera fixture the user wants to create.
 */
public class GuiFixturesPopup extends GuiElement
{
    public Consumer<AbstractFixture> callback;

    public GuiFixturesPopup(Minecraft mc, Consumer<AbstractFixture> callback)
    {
        super(mc);

        this.callback = callback;
        this.createChildren();

        int i = 0;

        for (FixtureInfo info : FixtureRegistry.CLIENT.values())
        {
            int color = 0xff000000 + info.color.getHex();
            GuiButtonElement<GuiButton> button = new GuiButtonElement<GuiButton>(mc, new GuiFlatButton(info.type, 0, 0, 0, 0, color, color - 0x00111111, I18n.format(info.title)), (b) ->
            {
                this.actionPerformed((byte) b.button.id);
            });

            button.resizer().parent(this.area).set(2, i * 20 + 2, 0, 20).w(1, -4);
            this.children.add(button);

            i++;
        }
    }

    @Override
    public void resize(int width, int height)
    {
        this.resizer().h(this.children.elements.size() * 20 + 4);

        super.resize(width, height);
    }

    /**
     * Select a fixture
     */
    private void actionPerformed(byte type)
    {
        long duration = Aperture.proxy.config.camera_duration;
        AbstractFixture fixture = null;

        try
        {
            fixture = FixtureRegistry.fromType(type, duration);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (fixture != null && this.callback != null)
        {
            this.callback.accept(fixture);
        }
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0xaa000000);

        super.draw(tooltip, mouseX, mouseY, partialTicks);
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
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
        {
            if (this.visible)
            {
                FontRenderer fontrenderer = mc.fontRenderer;
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

                int color = this.hovered ? this.activeColor : this.color;

                Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, color);
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

                this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
            }
        }
    }
}