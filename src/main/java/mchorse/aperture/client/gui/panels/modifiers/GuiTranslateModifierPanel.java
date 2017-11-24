package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.TranslateModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public class GuiTranslateModifierPanel extends GuiAbstractModifierPanel<TranslateModifier> implements ITrackpadListener
{
    public GuiTrackpad x;
    public GuiTrackpad y;
    public GuiTrackpad z;

    public GuiTranslateModifierPanel(TranslateModifier modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        super(modifier, modifiers, font);

        this.x = new GuiTrackpad(this, font);
        this.x.title = I18n.format("aperture.gui.panels.x");

        this.y = new GuiTrackpad(this, font);
        this.y.title = I18n.format("aperture.gui.panels.y");

        this.z = new GuiTrackpad(this, font);
        this.z.title = I18n.format("aperture.gui.panels.z");
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.x)
        {
            this.modifier.translate.x = value;
        }
        else if (trackpad == this.y)
        {
            this.modifier.translate.y = value;
        }
        else if (trackpad == this.z)
        {
            this.modifier.translate.z = value;
        }

        this.modifiers.editor.updateProfile();
    }

    @Override
    public void update(int x, int y, int w)
    {
        super.update(x, y, w);

        int width = (w - 20) / 2;

        this.x.update(x + 5, y + 25, width, 20);
        this.y.update(x + w - 5 - width, y + 25, width, 20);
        this.z.update(x + 5, y + 50, w - 10, 20);

        this.x.setValue(this.modifier.translate.x);
        this.y.setValue(this.modifier.translate.y);
        this.z.setValue(this.modifier.translate.z);
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
    public boolean hasActiveTextfields()
    {
        return this.x.text.isFocused() || this.y.text.isFocused() || this.z.text.isFocused();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        this.x.draw(mouseX, mouseY, partialTicks);
        this.y.draw(mouseX, mouseY, partialTicks);
        this.z.draw(mouseX, mouseY, partialTicks);
    }
}