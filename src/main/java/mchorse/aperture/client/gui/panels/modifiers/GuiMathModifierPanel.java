package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
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

        this.active = new GuiActiveWidget(mc, (value) ->
        {
            this.modifier.active = value;
            this.modifiers.editor.updateProfile();
        });

        this.math.resizer().parent(this.area).set(5, 25, 0, 20).w(1, -10);
        this.active.resizer().parent(this.area).set(5, 45, 0, 20).w(1, -10);

        this.children.add(this.math, this.active);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.math.setText(this.modifier.expression == null ? "" : this.modifier.expression.toString());
        this.active.value = this.modifier.active;
    }

    @Override
    public int getHeight()
    {
        return 70;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        if (!this.math.field.isFocused())
        {
            GuiUtils.drawRightString(font, I18n.format("aperture.gui.modifiers.math"), this.math.area.x + this.math.area.w - 4, this.math.area.y + 6, 0xffaaaaaa);
        }
    }
}