package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.Angle;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.panels.IGuiModule;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

/**
 * Angle GUI module
 *
 * This class unifies four trackpads into one object which edits a {@link Angle},
 * and makes it way easier to reuse in other classes.
 */
public class GuiAngleModule implements IGuiModule
{
    public GuiTrackpad yaw;
    public GuiTrackpad pitch;
    public GuiTrackpad roll;
    public GuiTrackpad fov;

    public GuiAngleModule(ITrackpadListener listener, FontRenderer font)
    {
        this.yaw = new GuiTrackpad(listener, font);
        this.pitch = new GuiTrackpad(listener, font);
        this.roll = new GuiTrackpad(listener, font);
        this.fov = new GuiTrackpad(listener, font);

        this.yaw.title = I18n.format("aperture.gui.panels.yaw");
        this.pitch.title = I18n.format("aperture.gui.panels.pitch");
        this.roll.title = I18n.format("aperture.gui.panels.roll");
        this.fov.title = I18n.format("aperture.gui.panels.fov");
    }

    public void fill(Angle angle)
    {
        this.yaw.setValue(angle.yaw);
        this.pitch.setValue(angle.pitch);
        this.roll.setValue(angle.roll);
        this.fov.setValue(angle.fov);
    }

    public void update(int x, int y)
    {
        this.yaw.update(x, y, 80, 20);
        this.pitch.update(x, y + 20, 80, 20);
        this.roll.update(x, y + 40, 80, 20);
        this.fov.update(x, y + 60, 80, 20);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.yaw.mouseClicked(mouseX, mouseY, mouseButton);
        this.pitch.mouseClicked(mouseX, mouseY, mouseButton);
        this.roll.mouseClicked(mouseX, mouseY, mouseButton);
        this.fov.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.yaw.mouseReleased(mouseX, mouseY, state);
        this.pitch.mouseReleased(mouseX, mouseY, state);
        this.roll.mouseReleased(mouseX, mouseY, state);
        this.fov.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.yaw.keyTyped(typedChar, keyCode);
        this.pitch.keyTyped(typedChar, keyCode);
        this.roll.keyTyped(typedChar, keyCode);
        this.fov.keyTyped(typedChar, keyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        this.yaw.draw(mouseX, mouseY, partialTicks);
        this.pitch.draw(mouseX, mouseY, partialTicks);
        this.roll.draw(mouseX, mouseY, partialTicks);
        this.fov.draw(mouseX, mouseY, partialTicks);
    }
}