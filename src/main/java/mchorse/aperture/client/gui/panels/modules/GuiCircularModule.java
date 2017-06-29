package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.panels.IGuiModule;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

/**
 * Circular GUI module
 *
 * This class unifies four trackpads into one object which edits
 * {@link CircularFixture}'s other properties, and makes it way easier to reuse
 * in other classes.
 */
public class GuiCircularModule implements IGuiModule
{
    public GuiTrackpad offset;
    public GuiTrackpad pitch;
    public GuiTrackpad circles;
    public GuiTrackpad distance;

    public GuiCircularModule(ITrackpadListener listener, FontRenderer font)
    {
        this.offset = new GuiTrackpad(listener, font);
        this.pitch = new GuiTrackpad(listener, font);
        this.circles = new GuiTrackpad(listener, font);
        this.distance = new GuiTrackpad(listener, font);

        this.offset.title = I18n.format("aperture.gui.panels.offset");
        this.pitch.title = I18n.format("aperture.gui.panels.pitch");
        this.circles.title = I18n.format("aperture.gui.panels.circles");
        this.distance.title = I18n.format("aperture.gui.panels.distance");
    }

    public void fill(CircularFixture fixture)
    {
        this.offset.setValue(fixture.offset);
        this.pitch.setValue(fixture.pitch);
        this.circles.setValue(fixture.circles);
        this.distance.setValue(fixture.distance);
    }

    public void update(int x, int y)
    {
        this.offset.update(x, y, 80, 20);
        this.pitch.update(x, y + 20, 80, 20);
        this.circles.update(x, y + 40, 80, 20);
        this.distance.update(x, y + 60, 80, 20);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.offset.mouseClicked(mouseX, mouseY, mouseButton);
        this.pitch.mouseClicked(mouseX, mouseY, mouseButton);
        this.circles.mouseClicked(mouseX, mouseY, mouseButton);
        this.distance.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.offset.mouseReleased(mouseX, mouseY, state);
        this.pitch.mouseReleased(mouseX, mouseY, state);
        this.circles.mouseReleased(mouseX, mouseY, state);
        this.distance.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.offset.keyTyped(typedChar, keyCode);
        this.pitch.keyTyped(typedChar, keyCode);
        this.circles.keyTyped(typedChar, keyCode);
        this.distance.keyTyped(typedChar, keyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        this.offset.draw(mouseX, mouseY, partialTicks);
        this.pitch.draw(mouseX, mouseY, partialTicks);
        this.circles.draw(mouseX, mouseY, partialTicks);
        this.distance.draw(mouseX, mouseY, partialTicks);
    }
}