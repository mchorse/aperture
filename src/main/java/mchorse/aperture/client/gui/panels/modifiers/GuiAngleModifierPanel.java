package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.AngleModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import net.minecraft.client.gui.FontRenderer;

public class GuiAngleModifierPanel extends GuiAbstractModifierPanel<AngleModifier> implements ITrackpadListener
{
    public GuiTrackpad yaw;
    public GuiTrackpad pitch;
    public GuiTrackpad roll;
    public GuiTrackpad fov;

    public GuiAngleModifierPanel(AngleModifier modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        super(modifier, modifiers, font);

        this.yaw = new GuiTrackpad(this, font);
        this.yaw.title = "Yaw";

        this.pitch = new GuiTrackpad(this, font);
        this.pitch.title = "Pitch";

        this.roll = new GuiTrackpad(this, font);
        this.roll.title = "Roll";

        this.fov = new GuiTrackpad(this, font);
        this.fov.title = "FOV";
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.yaw)
        {
            this.modifier.angle.yaw = value;
        }
        else if (trackpad == this.pitch)
        {
            this.modifier.angle.pitch = value;
        }
        else if (trackpad == this.roll)
        {
            this.modifier.angle.roll = value;
        }
        else if (trackpad == this.fov)
        {
            this.modifier.angle.fov = value;
        }

        this.modifiers.editor.updateProfile();
    }

    @Override
    public void update(int x, int y, int w)
    {
        super.update(x, y, w);

        int width = (w - 20) / 2;

        this.yaw.update(x + 5, y + 25, width, 20);
        this.pitch.update(x + w - width - 5, y + 25, width, 20);
        this.roll.update(x + 5, y + 50, width, 20);
        this.fov.update(x + w - width - 5, y + 50, width, 20);

        this.yaw.setValue(this.modifier.angle.yaw);
        this.pitch.setValue(this.modifier.angle.pitch);
        this.roll.setValue(this.modifier.angle.roll);
        this.fov.setValue(this.modifier.angle.fov);
    }

    @Override
    public int getHeight()
    {
        return 75;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

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
    {}

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        this.yaw.draw(mouseX, mouseY, partialTicks);
        this.pitch.draw(mouseX, mouseY, partialTicks);
        this.roll.draw(mouseX, mouseY, partialTicks);
        this.fov.draw(mouseX, mouseY, partialTicks);
    }
}