package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.OrbitModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.widgets.GuiTrackpad;
import mchorse.mclib.client.gui.widgets.GuiTrackpad.ITrackpadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiOrbitModifierPanel extends GuiAbstractModifierPanel<OrbitModifier> implements ITrackpadListener
{
    public GuiTrackpad yaw;
    public GuiTrackpad pitch;
    public GuiTrackpad distance;

    public GuiCheckBox copy;
    public GuiTextField selector;
    public String old = "";

    public GuiOrbitModifierPanel(OrbitModifier modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        super(modifier, modifiers, font);

        this.yaw = new GuiTrackpad(this, font);
        this.yaw.title = I18n.format("aperture.gui.panels.yaw");
        this.pitch = new GuiTrackpad(this, font);
        this.pitch.title = I18n.format("aperture.gui.panels.pitch");
        this.distance = new GuiTrackpad(this, font);
        this.distance.title = I18n.format("aperture.gui.panels.distance");

        this.copy = new GuiCheckBox(0, 0, 0, I18n.format("aperture.gui.modifiers.panels.copy_entity"), false);
        this.selector = new GuiTextField(0, font, 0, 0, 0, 0);
        this.selector.setMaxStringLength(500);
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.yaw)
        {
            this.modifier.yaw = value;
        }
        else if (trackpad == this.pitch)
        {
            this.modifier.pitch = value;
        }
        else if (trackpad == this.distance)
        {
            this.modifier.distance = value;
        }

        this.modifiers.editor.updateProfile();
    }

    @Override
    public void update(int x, int y, int w)
    {
        super.update(x, y, w);

        int width = (w - 20) / 2;

        this.yaw.update(x + 5, y + 50, width, 20);
        this.pitch.update(x + w - 5 - width, y + 50, width, 20);
        this.distance.update(x + 5, y + 75, width, 20);

        this.copy.xPosition = x + width + 15;
        this.copy.yPosition = y + 79;
        GuiUtils.setSize(this.selector, x + 5, y + 25, w - 10, 20);

        this.yaw.setValue(this.modifier.yaw);
        this.pitch.setValue(this.modifier.pitch);
        this.distance.setValue(this.modifier.distance);

        this.copy.setIsChecked(this.modifier.copy);
        this.selector.setText(this.modifier.selector);
        this.selector.setCursorPositionZero();
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

        this.yaw.mouseClicked(mouseX, mouseY, mouseButton);
        this.pitch.mouseClicked(mouseX, mouseY, mouseButton);
        this.distance.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.copy.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY))
        {
            this.modifier.copy = this.copy.isChecked();
        }

        this.selector.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.yaw.mouseReleased(mouseX, mouseY, state);
        this.pitch.mouseReleased(mouseX, mouseY, state);
        this.distance.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.yaw.keyTyped(typedChar, keyCode);
        this.pitch.keyTyped(typedChar, keyCode);
        this.distance.keyTyped(typedChar, keyCode);

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
        return this.selector.isFocused() || this.yaw.text.isFocused() || this.pitch.text.isFocused() || this.distance.text.isFocused();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        Minecraft mc = Minecraft.getMinecraft();

        this.yaw.draw(mouseX, mouseY, partialTicks);
        this.pitch.draw(mouseX, mouseY, partialTicks);
        this.distance.draw(mouseX, mouseY, partialTicks);

        this.copy.drawButton(mc, mouseX, mouseY);
        this.selector.drawTextBox();

        if (!this.selector.isFocused())
        {
            GuiUtils.drawRightString(font, I18n.format("aperture.gui.panels.selector"), this.selector.xPosition + this.selector.width - 4, this.selector.yPosition + 5, 0xffaaaaaa);
        }
    }
}