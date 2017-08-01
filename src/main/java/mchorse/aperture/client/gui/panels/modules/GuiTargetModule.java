package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.utils.GuiUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

/**
 * Target GUI module
 *
 * This class unifies two text fields into one object which edits
 * {@link LookFixture}'s target based properties, and makes it way easier to
 * reuse in other classes.
 */
public class GuiTargetModule implements IGuiModule
{
    public GuiTextField selector;

    public FontRenderer font;

    public GuiTargetModule(GuiResponder responder, FontRenderer font, int selector)
    {
        this.selector = new GuiTextField(selector, font, 0, 0, 0, 0);
        this.selector.setGuiResponder(responder);

        this.font = font;
    }

    public void fill(LookFixture fixture)
    {
        this.selector.setMaxStringLength(200);

        this.selector.setText(fixture.selector);
        this.selector.setCursorPositionZero();
    }

    public void update(int x, int y)
    {
        this.selector.xPosition = x + 1;
        this.selector.yPosition = y + 1;

        this.selector.width = 98;
        this.selector.height = 18;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.selector.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

    public boolean hasActiveTextfields()
    {
        return this.selector.isFocused();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.selector.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        this.selector.drawTextBox();

        if (!this.selector.isFocused())
        {
            GuiUtils.drawRightString(this.font, I18n.format("aperture.gui.panels.selector"), this.selector.xPosition + this.selector.width - 4, this.selector.yPosition + 5, 0xffaaaaaa);
        }
    }
}