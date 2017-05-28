package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.utils.GuiUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiTextField;

/**
 * Target GUI module
 *
 * This class unifies two text fields into one object which edits
 * {@link LookFixture}'s target based properties, and makes it way easier to
 * reuse in other classes.
 */
public class GuiTargetModule implements IGuiModule
{
    public GuiTextField target;
    public GuiTextField selector;

    public FontRenderer font;

    public GuiTargetModule(GuiResponder responder, FontRenderer font, int target, int selector)
    {
        this.target = new GuiTextField(target, font, 0, 0, 0, 0);
        this.target.setGuiResponder(responder);

        this.selector = new GuiTextField(selector, font, 0, 0, 0, 0);
        this.selector.setGuiResponder(responder);

        this.font = font;
    }

    public void fill(LookFixture fixture)
    {
        this.target.setMaxStringLength(80);
        this.selector.setMaxStringLength(200);

        this.target.setText(fixture.target);
        this.target.setCursorPositionZero();
        this.selector.setText(fixture.selector);
        this.selector.setCursorPositionZero();
    }

    public void update(int x, int y)
    {
        this.target.xPosition = x + 1;
        this.selector.xPosition = x + 1;

        this.target.yPosition = y;
        this.selector.yPosition = y + 25;

        this.target.width = this.selector.width = 98;
        this.target.height = this.selector.height = 18;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.target.mouseClicked(mouseX, mouseY, mouseButton);
        this.selector.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.target.textboxKeyTyped(typedChar, keyCode);
        this.selector.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        this.target.drawTextBox();
        this.selector.drawTextBox();

        if (!this.target.isFocused())
        {
            GuiUtils.drawRightString(this.font, "Target", this.target.xPosition + this.target.width - 4, this.target.yPosition + 5, 0xffaaaaaa);
        }

        if (!this.selector.isFocused())
        {
            GuiUtils.drawRightString(this.font, "Selector", this.selector.xPosition + this.selector.width - 4, this.selector.yPosition + 5, 0xffaaaaaa);
        }
    }
}