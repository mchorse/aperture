package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiMathModifierPanel extends GuiAbstractModifierPanel<MathModifier>
{
    public GuiTextField math;

    public GuiMathModifierPanel(MathModifier modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        super(modifier, modifiers, font);

        this.title = "Math";
        this.math = new GuiTextField(0, font, 0, 0, 148, 18);
        this.math.setMaxStringLength(500);
    }

    @Override
    public void update(int x, int y)
    {
        super.update(x, y);

        GuiUtils.setSize(this.math, x - 155, y + 20, 150, 20);

        this.math.setText(this.modifier.value != null ? this.modifier.value.toString() : "");
        this.math.setCursorPositionZero();
        this.math.setMaxStringLength(500);
    }

    @Override
    public int getHeight()
    {
        return 50;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.math.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.math.textboxKeyTyped(typedChar, keyCode);

        if (this.math.isFocused() && !this.math.getText().isEmpty())
        {
            this.modifier.rebuildExpression(this.math.getText());
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
    }
}