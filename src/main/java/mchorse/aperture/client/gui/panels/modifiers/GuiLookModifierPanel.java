package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.LookModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiLookModifierPanel extends GuiAbstractModifierPanel<LookModifier>
{
    public GuiTextElement selector;

    public GuiTrackpadElement x;
    public GuiTrackpadElement y;
    public GuiTrackpadElement z;

    public GuiButtonElement<GuiCheckBox> relative;
    public GuiButtonElement<GuiCheckBox> atBlock;
    public GuiButtonElement<GuiCheckBox> forward;

    public GuiLookModifierPanel(Minecraft mc, LookModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.selector = new GuiTextElement(mc, 500, (str) ->
        {
            this.modifier.selector = str;
            this.modifier.tryFindingEntity();
            this.modifiers.editor.updateProfile();
        });

        this.x = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.x"), (value) ->
        {
            this.modifier.block.x = value;
            this.modifiers.editor.updateProfile();
        });

        this.y = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.y"), (value) ->
        {
            this.modifier.block.y = value;
            this.modifiers.editor.updateProfile();
        });

        this.z = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.z"), (value) ->
        {
            this.modifier.block.z = value;
            this.modifiers.editor.updateProfile();
        });

        this.relative = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.modifiers.panels.relative"), false, (b) ->
        {
            this.modifier.relative = b.button.isChecked();
            this.modifiers.editor.updateProfile();
        });

        this.atBlock = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.modifiers.panels.at_block"), false, (b) ->
        {
            this.modifier.atBlock = b.button.isChecked();
            this.updateVisibility();
            this.modifiers.editor.updateProfile();
        });

        this.forward = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.modifiers.panels.forward"), false, (b) ->
        {
            this.modifier.forward = b.button.isChecked();
            this.modifiers.editor.updateProfile();
        });

        this.selector.resizer().parent(this.area).set(5, 25, 0, 20).w(1, -10);
        this.x.resizer().parent(this.area).set(5, 25, 0, 20).w(0.5F, -10);
        this.y.resizer().parent(this.area).set(5, 25, 0, 20).x(0.5F, 5).w(0.5F, -10);
        this.z.resizer().parent(this.area).set(5, 50, 0, 20).w(1, -10);
        this.relative.resizer().parent(this.area).set(5, 75, this.relative.button.width, 11);
        this.atBlock.resizer().parent(this.area).set(5, 75, this.atBlock.button.width, 11).x(0.5F, 5);
        this.forward.resizer().parent(this.area).set(5, 91, this.forward.button.width, 11);

        this.children.add(this.selector, this.x, this.y, this.z, this.relative, this.atBlock, this.forward);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.selector.setText(this.modifier.selector);
        this.x.setValue((float) this.modifier.block.x);
        this.y.setValue((float) this.modifier.block.y);
        this.z.setValue((float) this.modifier.block.z);
        this.relative.button.setIsChecked(this.modifier.relative);
        this.atBlock.button.setIsChecked(this.modifier.atBlock);
        this.forward.button.setIsChecked(this.modifier.forward);

        this.updateVisibility();
    }

    private void updateVisibility()
    {
        boolean atBlock = this.modifier.atBlock;

        this.selector.setVisible(!atBlock);
        this.x.setVisible(atBlock);
        this.y.setVisible(atBlock);
        this.z.setVisible(atBlock);
    }

    @Override
    public int getHeight()
    {
        return 107;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        if (this.selector.isVisible() && !this.selector.field.isFocused())
        {
            GuiUtils.drawRightString(font, I18n.format("aperture.gui.panels.selector"), this.selector.area.x + this.selector.area.w - 4, this.selector.area.y + 5, 0xffaaaaaa);
        }
    }
}