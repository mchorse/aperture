package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiMathModifierPanel extends GuiAbstractModifierPanel<MathModifier>
{
    public GuiTextElement math;
    public GuiActiveWidget active;

    public GuiMathModifierPanel(Minecraft mc, MathModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.math = new GuiTextElement(mc, 500, (str) ->
        {
            this.math.field.setTextColor(this.modifier.rebuildExpression(str) ? 0xffffff : 0xff2244);
            this.modifiers.editor.updateProfile();
        });
        this.math.tooltip(I18n.format("aperture.gui.modifiers.math"));

        this.active = new GuiActiveWidget(mc, (value) ->
        {
            this.modifier.active = value;
            this.modifiers.editor.updateProfile();
        });

        this.fields.add(this.math, this.active);
    }

    @Override
    public void resize()
    {
        super.resize();

        this.math.setText(this.modifier.expression == null ? "" : this.modifier.expression.toString());
        this.active.value = this.modifier.active;
    }

    @Override
    public void draw(GuiContext context)
    {
        super.draw(context);
    }
}