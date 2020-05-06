package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.FollowModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiFollowModifierPanel extends GuiAbstractModifierPanel<FollowModifier>
{
    public GuiTextElement selector;
    public GuiToggleElement relative;

    public GuiFollowModifierPanel(Minecraft mc, FollowModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.selector = new GuiTextElement(mc, 500, (str) ->
        {
            this.modifier.selector = str;
            this.modifier.tryFindingEntity();
            this.modifiers.editor.updateProfile();
        });
        this.selector.tooltip(IKey.lang("aperture.gui.panels.selector"));

        this.relative = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.relative"), false, (b) ->
        {
            this.modifier.relative = b.isToggled();
            this.modifiers.editor.updateProfile();
        });

        this.fields.add(this.selector, this.relative);
    }

    @Override
    public void resize()
    {
        super.resize();

        this.selector.setText(this.modifier.selector);
        this.relative.toggled(this.modifier.relative);
    }
}