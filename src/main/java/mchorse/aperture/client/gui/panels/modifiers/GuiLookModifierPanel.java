package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.LookModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiLookModifierPanel extends GuiAbstractModifierPanel<LookModifier>
{
    public GuiTextElement selector;

    public GuiTrackpadElement x;
    public GuiTrackpadElement y;
    public GuiTrackpadElement z;

    public GuiToggleElement relative;
    public GuiToggleElement atBlock;
    public GuiToggleElement forward;

    public GuiElement row;

    public GuiLookModifierPanel(Minecraft mc, LookModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.selector = new GuiTextElement(mc, 500, (str) ->
        {
            this.modifier.selector = str;
            this.modifier.tryFindingEntity();
            this.modifiers.editor.updateProfile();
        });
        this.selector.tooltip(IKey.lang("aperture.gui.panels.selector"));

        this.x = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.block.x = value;
            this.modifiers.editor.updateProfile();
        });
        this.x.tooltip(IKey.lang("aperture.gui.panels.x"));

        this.y = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.block.y = value;
            this.modifiers.editor.updateProfile();
        });
        this.y.tooltip(IKey.lang("aperture.gui.panels.y"));

        this.z = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.block.z = value;
            this.modifiers.editor.updateProfile();
        });
        this.z.tooltip(IKey.lang("aperture.gui.panels.z"));

        this.relative = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.relative"), false, (b) ->
        {
            this.modifier.relative = b.isToggled();
            this.modifiers.editor.updateProfile();
        });

        this.atBlock = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.at_block"), false, (b) ->
        {
            this.modifier.atBlock = b.isToggled();
            this.updateVisibility(true);
            this.modifiers.editor.updateProfile();
        });

        this.forward = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.forward"), false, (b) ->
        {
            this.modifier.forward = b.isToggled();
            this.modifiers.editor.updateProfile();
        });

        this.row = Elements.row(mc, 5, 0, 20, this.x, this.y, this.z);
        this.updateVisibility(false);
        this.fields.add(Elements.row(mc, 5, 0, 20,  this.relative, this.atBlock), this.forward);
    }

    @Override
    public void resize()
    {
        super.resize();

        this.selector.setText(this.modifier.selector);
        this.x.setValue((float) this.modifier.block.x);
        this.y.setValue((float) this.modifier.block.y);
        this.z.setValue((float) this.modifier.block.z);
        this.relative.toggled(this.modifier.relative);
        this.atBlock.toggled(this.modifier.atBlock);
        this.forward.toggled(this.modifier.forward);

        this.updateVisibility(false);
    }

    private void updateVisibility(boolean resize)
    {
        boolean atBlock = this.modifier.atBlock;

        this.row.removeFromParent();
        this.selector.removeFromParent();

        if (atBlock)
        {
            this.fields.prepend(this.row);
        }
        else
        {
            this.fields.prepend(this.selector);
        }

        if (resize)
        {
            this.getParent().resize();
        }
    }
}