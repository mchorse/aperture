package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.LookModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiLookModifierPanel extends GuiAbstractModifierPanel<LookModifier> implements ITrackpadListener
{
    public GuiTextField selector;
    public String old = "";

    public GuiTrackpad x;
    public GuiTrackpad y;
    public GuiTrackpad z;

    public GuiCheckBox relative;
    public GuiCheckBox atBlock;

    public GuiLookModifierPanel(LookModifier modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        super(modifier, modifiers, font);

        this.selector = new GuiTextField(0, font, 0, 0, 0, 0);
        this.selector.setMaxStringLength(500);

        this.x = new GuiTrackpad(this, font);
        this.x.title = I18n.format("aperture.gui.panels.x");

        this.y = new GuiTrackpad(this, font);
        this.y.title = I18n.format("aperture.gui.panels.y");

        this.z = new GuiTrackpad(this, font);
        this.z.title = I18n.format("aperture.gui.panels.z");

        this.relative = new GuiCheckBox(0, 0, 0, I18n.format("aperture.gui.modifiers.panels.relative"), false);
        this.atBlock = new GuiCheckBox(0, 0, 0, I18n.format("aperture.gui.modifiers.panels.at_block"), false);
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.x)
        {
            this.modifier.block.x = value;
        }
        else if (trackpad == this.y)
        {
            this.modifier.block.y = value;
        }
        else if (trackpad == this.z)
        {
            this.modifier.block.z = value;
        }

        this.modifiers.editor.updateProfile();
    }

    @Override
    public void update(int x, int y, int w)
    {
        super.update(x, y, w);

        GuiUtils.setSize(this.selector, x + 5, y + 25, w - 10, 20);

        int width = (w - 20) / 2;

        this.selector.setText(this.modifier.selector);
        this.selector.setCursorPositionZero();

        this.x.update(x + 5, y + 25, width, 20);
        this.y.update(x + w - 5 - width, y + 25, width, 20);
        this.z.update(x + 5, y + 50, w - 10, 20);

        this.relative.x = x + 5;
        this.relative.y = y + 80;
        this.atBlock.x = x + 10 + width;
        this.atBlock.y = y + 80;

        this.x.setValue(this.modifier.block.x);
        this.y.setValue(this.modifier.block.y);
        this.z.setValue(this.modifier.block.z);
        this.relative.setIsChecked(this.modifier.relative);
        this.atBlock.setIsChecked(this.modifier.atBlock);
    }

    @Override
    public int getHeight()
    {
        return 100;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.modifier.atBlock)
        {
            this.x.mouseClicked(mouseX, mouseY, mouseButton);
            this.y.mouseClicked(mouseX, mouseY, mouseButton);
            this.z.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else
        {
            this.selector.mouseClicked(mouseX, mouseY, mouseButton);
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (this.relative.mousePressed(mc, mouseX, mouseY))
        {
            this.modifier.relative = this.relative.isChecked();
            this.modifiers.editor.updateProfile();
        }
        else if (this.atBlock.mousePressed(mc, mouseX, mouseY))
        {
            this.modifier.atBlock = this.atBlock.isChecked();
            this.modifiers.editor.updateProfile();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (this.modifier.atBlock)
        {
            this.x.mouseReleased(mouseX, mouseY, state);
            this.y.mouseReleased(mouseX, mouseY, state);
            this.z.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (this.modifier.atBlock)
        {
            this.x.keyTyped(typedChar, keyCode);
            this.y.keyTyped(typedChar, keyCode);
            this.z.keyTyped(typedChar, keyCode);
        }
        else
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
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return this.selector.isFocused() || this.x.text.isFocused() || this.y.text.isFocused() || this.z.text.isFocused();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        if (this.modifier.atBlock)
        {
            this.x.draw(mouseX, mouseY, partialTicks);
            this.y.draw(mouseX, mouseY, partialTicks);
            this.z.draw(mouseX, mouseY, partialTicks);
        }
        else
        {
            this.selector.drawTextBox();

            if (!this.selector.isFocused())
            {
                GuiUtils.drawRightString(font, I18n.format("aperture.gui.panels.selector"), this.selector.x + this.selector.width - 4, this.selector.y + 5, 0xffaaaaaa);
            }
        }

        Minecraft mc = Minecraft.getMinecraft();

        this.relative.drawButton(mc, mouseX, mouseY, partialTicks);
        this.atBlock.drawButton(mc, mouseX, mouseY, partialTicks);
    }
}