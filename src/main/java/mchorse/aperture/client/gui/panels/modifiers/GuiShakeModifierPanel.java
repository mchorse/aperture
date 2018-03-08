package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.ShakeModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public class GuiShakeModifierPanel extends GuiAbstractModifierPanel<ShakeModifier> implements ITrackpadListener
{
    public GuiTrackpad shake;
    public GuiTrackpad shakeAmount;
    public GuiActiveWidget active;

    public GuiShakeModifierPanel(ShakeModifier modifier, GuiModifiersManager panel, FontRenderer font)
    {
        super(modifier, panel, font);

        this.shake = new GuiTrackpad(this, font);
        this.shakeAmount = new GuiTrackpad(this, font);
        this.active = new GuiActiveWidget();

        this.shake.title = I18n.format("aperture.gui.modifiers.panels.shake");
        this.shakeAmount.title = I18n.format("aperture.gui.modifiers.panels.shake_amount");
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.shake && value != 0)
        {
            this.modifier.shake = value;
            this.modifiers.editor.updateProfile();
        }
        else if (trackpad == this.shakeAmount)
        {
            this.modifier.shakeAmount = value;
            this.modifiers.editor.updateProfile();
        }
    }

    @Override
    public void update(int x, int y, int w)
    {
        super.update(x, y, w);

        int width = ((w - 20) / 2);

        this.shake.update(x + 5, y + 25, width, 20);
        this.shakeAmount.update(x + w - 5 - width, y + 25, width, 20);

        this.shake.setValue(this.modifier.shake);
        this.shakeAmount.setValue(this.modifier.shakeAmount);

        this.active.area.set(x + 5, y + 50, w - 10, 20);
        this.active.value = this.modifier.active;
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

        this.shake.mouseClicked(mouseX, mouseY, mouseButton);
        this.shakeAmount.mouseClicked(mouseX, mouseY, mouseButton);
        this.active.mouseClicked(mouseX, mouseY, mouseButton);

        byte active = this.modifier.active;

        this.modifier.active = this.active.value;

        if (active != this.modifier.active)
        {
            this.modifiers.editor.updateProfile();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.shake.mouseReleased(mouseX, mouseY, state);
        this.shakeAmount.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.shake.keyTyped(typedChar, keyCode);
        this.shakeAmount.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return this.shake.text.isFocused() || this.shakeAmount.text.isFocused();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        this.shake.draw(mouseX, mouseY, partialTicks);
        this.shakeAmount.draw(mouseX, mouseY, partialTicks);
        this.active.draw(mouseX, mouseY, partialTicks);
    }
}