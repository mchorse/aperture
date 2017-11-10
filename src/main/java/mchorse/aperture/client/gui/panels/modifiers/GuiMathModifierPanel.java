package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.aperture.client.gui.utils.GuiUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiMathModifierPanel extends GuiAbstractModifierPanel<MathModifier>
{
    public GuiTextField math;
    public GuiActiveWidget active;
    public String old = "";

    public GuiMathModifierPanel(MathModifier modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        super(modifier, modifiers, font);

        this.math = new GuiTextField(0, font, 0, 0, 148, 18);
        this.math.setMaxStringLength(500);

        this.active = new GuiActiveWidget();
    }

    @Override
    public void update(int x, int y, int w)
    {
        super.update(x, y, w);

        GuiUtils.setSize(this.math, x + 5, y + 25, w - 10, 20);

        this.math.setText(this.modifier.expression != null ? this.modifier.expression.toString() : "");
        this.math.setCursorPositionZero();

        this.active.area.set(x + 5, y + 45, w - 10, 20);
        this.active.value = this.modifier.active;
    }

    @Override
    public int getHeight()
    {
        return 70;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.math.mouseClicked(mouseX, mouseY, mouseButton);
        this.active.mouseClicked(mouseX, mouseY, mouseButton);

        this.modifier.active = this.active.value;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.math.textboxKeyTyped(typedChar, keyCode);

        String text = this.math.getText();

        if (this.math.isFocused() && !text.equals(this.old) && !text.isEmpty())
        {
            this.math.setTextColor(this.modifier.rebuildExpression(text) ? 0xffffff : 0xff2244);
            this.modifiers.editor.updateProfile();
        }
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return this.math.isFocused();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        this.math.drawTextBox();
        this.active.draw(mouseX, mouseY, partialTicks);
    }
}