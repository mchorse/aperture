package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.Point;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.panels.IGuiModule;
import net.minecraft.client.gui.FontRenderer;

/**
 * Point GUI module
 *
 * This class unifies three trackpads into one object which edits a {@link Point},
 * and makes it way easier to reuse in other classes.
 */
public class GuiPointModule implements IGuiModule
{
    public GuiTrackpad x;
    public GuiTrackpad y;
    public GuiTrackpad z;

    public GuiPointModule(ITrackpadListener listener, FontRenderer font)
    {
        this.x = new GuiTrackpad(listener, font);
        this.y = new GuiTrackpad(listener, font);
        this.z = new GuiTrackpad(listener, font);

        this.x.title = "X";
        this.y.title = "Y";
        this.z.title = "Z";

        this.x.amplitude = this.y.amplitude = this.z.amplitude = 0.1F;
    }

    public void fill(Point point)
    {
        this.x.setValue(point.x);
        this.y.setValue(point.y);
        this.z.setValue(point.z);
    }

    public void update(int x, int y)
    {
        this.x.update(x - 80, y, 80, 20);
        this.y.update(x - 80, y + 30, 80, 20);
        this.z.update(x - 80, y + 60, 80, 20);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.x.mouseClicked(mouseX, mouseY, mouseButton);
        this.y.mouseClicked(mouseX, mouseY, mouseButton);
        this.z.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.x.mouseReleased(mouseX, mouseY, state);
        this.y.mouseReleased(mouseX, mouseY, state);
        this.z.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.x.keyTyped(typedChar, keyCode);
        this.y.keyTyped(typedChar, keyCode);
        this.z.keyTyped(typedChar, keyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        this.x.draw(mouseX, mouseY, partialTicks);
        this.y.draw(mouseX, mouseY, partialTicks);
        this.z.draw(mouseX, mouseY, partialTicks);
    }
}