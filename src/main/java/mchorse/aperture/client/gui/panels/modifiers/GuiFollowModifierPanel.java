package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.FollowModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiFollowModifierPanel extends GuiAbstractModifierPanel<FollowModifier>
{
    public GuiTextElement selector;

    public GuiFollowModifierPanel(Minecraft mc, FollowModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.selector = new GuiTextElement(mc, 500, (str) ->
        {
            this.modifier.selector = str;
            this.modifier.tryFindingEntity();
            this.modifiers.editor.updateProfile();
        });

        this.selector.resizer().parent(this.area).set(5, 25, 0, 20).w(1, -10);

        this.children.add(this.selector);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.selector.setText(this.modifier.selector);
    }

    @Override
    public int getHeight()
    {
        return 50;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        if (!this.selector.field.isFocused())
        {
            GuiUtils.drawRightString(font, I18n.format("aperture.gui.panels.selector"), this.selector.area.x + this.selector.area.w - 4, this.selector.area.y + 5, 0xffaaaaaa);
        }
    }
}