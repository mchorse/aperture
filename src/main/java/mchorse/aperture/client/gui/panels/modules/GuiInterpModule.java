package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.InterpolationType;
import mchorse.aperture.client.gui.panels.IButtonListener;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.widgets.GuiButtonList;
import mchorse.aperture.client.gui.widgets.buttons.GuiCirculate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Path fixture interpolations GUI module
 *
 * This module is responsible for changing angle and/or position interpolation
 * of the path fixture.
 */
public class GuiInterpModule implements IGuiModule
{
    public GuiButtonList buttons;

    public GuiCirculate pos;
    public GuiCirculate angle;

    private Minecraft mc;

    public GuiInterpModule(IButtonListener listener)
    {
        this.buttons = new GuiButtonList(Minecraft.getMinecraft(), listener);

        this.pos = new GuiCirculate(-1, 0, 0, 0, 0);
        this.angle = new GuiCirculate(-2, 0, 0, 0, 0);

        this.pos.addLabel("Linear");
        this.pos.addLabel("Cubic");
        this.pos.addLabel("Hermite");

        this.angle.addLabel("Linear");
        this.angle.addLabel("Cubic");
        this.angle.addLabel("Hermite");

        this.mc = Minecraft.getMinecraft();
        this.buttons.add(this.pos);
        this.buttons.add(this.angle);
    }

    public void fill(PathFixture fixture)
    {
        this.pos.setValue(this.indexFromInterpType(fixture.interpolationPos));
        this.angle.setValue(this.indexFromInterpType(fixture.interpolationAngle));
    }

    public void update(int x, int y, int w)
    {
        this.pos.width = this.angle.width = w;
        this.pos.height = this.angle.height = 20;
        this.pos.xPosition = this.angle.xPosition = x;

        this.pos.yPosition = y;
        this.angle.yPosition = y + 25;
    }

    /**
     * Get the index of interpolation type
     */
    protected int indexFromInterpType(InterpolationType type)
    {
        if (type == InterpolationType.CUBIC)
        {
            return 1;
        }

        if (type == InterpolationType.HERMITE)
        {
            return 2;
        }

        return 0;
    }

    /**
     * Get interpolation type from an index
     */
    public InterpolationType typeFromIndex(int index)
    {
        if (index == 1)
        {
            return InterpolationType.CUBIC;
        }

        if (index == 2)
        {
            return InterpolationType.HERMITE;
        }

        return InterpolationType.LINEAR;
    }

    /**
     * Mouse clicked
     *
     * This method is responsible for detecting which button was pressed and
     * notification of the listener when it is pressed.
     */
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.buttons.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {}

    /**
     * Just draw the buttons
     */
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        this.buttons.draw(mouseX, mouseY);

        FontRenderer font = this.mc.fontRendererObj;

        font.drawStringWithShadow("Position", this.pos.xPosition + this.pos.width + 5, this.pos.yPosition + 6, 0xffffff);
        font.drawStringWithShadow("Angle", this.angle.xPosition + this.angle.width + 5, this.angle.yPosition + 6, 0xffffff);
    }
}