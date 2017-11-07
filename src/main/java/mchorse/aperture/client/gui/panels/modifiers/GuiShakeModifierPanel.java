package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.ShakeModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import net.minecraft.client.gui.FontRenderer;

public class GuiShakeModifierPanel extends GuiAbstractModifierPanel<ShakeModifier> implements ITrackpadListener
{
    public GuiTrackpad shake;
    public GuiTrackpad shakeAmount;

    public GuiShakeModifierPanel(ShakeModifier modifier, GuiModifiersManager panel, FontRenderer font)
    {
        super(modifier, panel, font);

        this.shake = new GuiTrackpad(this, font);
        this.shakeAmount = new GuiTrackpad(this, font);

        /* TODO: extract strings */
        this.shake.title = "Shake";
        this.shakeAmount.title = "Amount";
        this.title = "Camera shake";
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

    /**
     * TODO: rewrite to also accept width (and left top corner instead 
     * of right top) 
     */
    @Override
    public void update(int x, int y)
    {
        super.update(x, y);

        this.shake.update(x - 160 + 5, y + 20, 70, 20);
        this.shakeAmount.update(x - 75, y + 20, 70, 20);

        this.shake.setValue(this.modifier.shake);
        this.shakeAmount.setValue(this.modifier.shakeAmount);
    }

    @Override
    public int getHeight()
    {
        return 45;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.shake.mouseClicked(mouseX, mouseY, mouseButton);
        this.shakeAmount.mouseClicked(mouseX, mouseY, mouseButton);
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
    }
}