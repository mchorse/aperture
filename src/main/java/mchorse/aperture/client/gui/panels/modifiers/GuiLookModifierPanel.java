package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.LookModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiLookModifierPanel extends GuiAbstractModifierPanel<LookModifier>
{
    public GuiTextField selector;
    public String old = "";

    public GuiLookModifierPanel(LookModifier modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        super(modifier, modifiers, font);

        this.title = "Look";
        this.selector = new GuiTextField(0, font, 0, 0, 0, 0);
        this.selector.setMaxStringLength(500);
    }

    @Override
    public void update(int x, int y, int w)
    {
        super.update(x, y, w);

        GuiUtils.setSize(this.selector, x + 5, y + 25, w - 10, 20);

        this.selector.setText(this.modifier.selector);
        this.selector.setCursorPositionZero();
    }

    @Override
    public int getHeight()
    {
        return 50;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.selector.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.selector.textboxKeyTyped(typedChar, keyCode);

        String text = this.selector.getText();

        if (this.selector.isFocused() && !text.equals(this.old) && !text.isEmpty())
        {
            this.modifier.selector = text;
            this.modifier.tryFindingEntity();
            this.modifiers.editor.updateProfile();
        }
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return this.selector.isFocused();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        this.selector.drawTextBox();
    }
}