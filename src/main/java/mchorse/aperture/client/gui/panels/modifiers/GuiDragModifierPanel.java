package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.DragModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.mclib.client.gui.widgets.GuiTrackpad;
import mchorse.mclib.client.gui.widgets.GuiTrackpad.ITrackpadListener;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public class GuiDragModifierPanel extends GuiAbstractModifierPanel<DragModifier> implements ITrackpadListener
{
    public GuiTrackpad factor;
    public GuiActiveWidget active;

    public GuiDragModifierPanel(DragModifier modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        super(modifier, modifiers, font);

        this.factor = new GuiTrackpad(this, font);
        this.factor.max = 1;
        this.factor.min = 0;
        this.factor.amplitude = 0.05F;
        this.factor.title = I18n.format("aperture.gui.modifiers.panels.factor");

        this.active = new GuiActiveWidget();
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        this.modifier.factor = value;
    }

    @Override
    public void update(int x, int y, int w)
    {
        super.update(x, y, w);

        int width = (w - 20);

        this.factor.update(x + 5, y + 25, width, 20);
        this.factor.setValue(this.modifier.factor);

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

        this.factor.mouseClicked(mouseX, mouseY, mouseButton);
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
        this.factor.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.factor.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return this.factor.text.isFocused();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        this.factor.draw(mouseX, mouseY, partialTicks);
        this.active.draw(mouseX, mouseY, partialTicks);
    }
}