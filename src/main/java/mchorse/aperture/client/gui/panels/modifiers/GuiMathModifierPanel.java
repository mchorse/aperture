package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.aperture.client.gui.utils.GuiTextHelpElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiMathModifierPanel extends GuiAbstractModifierPanel<MathModifier>
{
    public GuiTextHelpElement expression;
    public GuiActiveWidget active;
    public GuiIconElement help;

    public GuiMathModifierPanel(Minecraft mc, MathModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.expression = new GuiTextHelpElement(mc, 500, (str) ->
        {
            this.modifiers.editor.postUndo(this.undo(this.modifier.expression, str));
            this.expression.field.setTextColor(!this.modifier.expression.isErrored() ? 0xffffff : 0xff2244);
        });
        this.expression.link("https://github.com/mchorse/aperture/wiki/Math-Expressions").tooltip(IKey.lang("aperture.gui.modifiers.panels.math"));

        this.active = new GuiActiveWidget(mc, (value) -> this.modifiers.editor.postUndo(this.undo(this.modifier.active, value)));

        this.fields.add(this.expression, this.active);
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.expression.setText(this.modifier.expression.toString());
        this.expression.field.setTextColor(0xffffff);
        this.active.value = this.modifier.active.get();
    }
}