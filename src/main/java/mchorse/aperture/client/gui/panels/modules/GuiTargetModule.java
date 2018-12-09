package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Target GUI module
 *
 * This class unifies two text fields into one object which edits
 * {@link LookFixture}'s target based properties, and makes it way easier to
 * reuse in other classes.
 */
public class GuiTargetModule extends GuiAbstractModule
{
    public GuiTextElement selector;
    public LookFixture fixture;

    public GuiTargetModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.selector = new GuiTextElement(mc, 200, (str) ->
        {
            this.fixture.selector = str;
            this.fixture.tryFindingEntity();
            this.editor.updateProfile();
        });

        this.selector.resizer().parent(this.area).set(0, 0, 0, 20).w(1, 0);

        this.children.add(this.selector);
    }

    public void fill(LookFixture fixture)
    {
        this.fixture = fixture;

        this.selector.setText(fixture.selector);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        if (!this.selector.field.isFocused())
        {
            GuiUtils.drawRightString(this.font, I18n.format("aperture.gui.panels.selector"), this.selector.area.x + this.selector.area.w - 4, this.selector.area.y + 6, 0xffaaaaaa);
        }
    }
}