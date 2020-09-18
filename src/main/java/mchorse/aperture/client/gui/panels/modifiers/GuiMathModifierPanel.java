package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.aperture.client.gui.utils.GuiTextHelpElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiMathModifierPanel extends GuiAbstractModifierPanel<MathModifier>
{
    public GuiTextHelpElement math;
    public GuiActiveWidget active;
    public GuiIconElement help;

    public GuiMathModifierPanel(Minecraft mc, MathModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.math = new GuiTextHelpElement(mc, 500, (str) ->
        {
            this.math.field.setTextColor(this.modifier.rebuildExpression(str) ? 0xffffff : 0xff2244);
            this.modifiers.editor.updateProfile();
        });
        this.math.link("https://github.com/mchorse/aperture/wiki/Math-Expressions").tooltip(IKey.lang("aperture.gui.modifiers.panels.math"));

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