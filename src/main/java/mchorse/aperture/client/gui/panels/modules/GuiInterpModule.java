package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.InterpolationType;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.widgets.buttons.GuiCirculate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Path fixture interpolations GUI module
 *
 * This module is responsible for changing angle and/or position interpolation
 * of the path fixture.
 */
public class GuiInterpModule extends GuiAbstractModule
{
    public GuiButtonElement<GuiCirculate> pos;
    public GuiButtonElement<GuiCirculate> angle;

    public PathFixture fixture;

    public GuiInterpModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.pos = new GuiButtonElement<GuiCirculate>(mc, new GuiCirculate(0, 0, 0, 0, 0), (b) ->
        {
            this.fixture.interpolationPos = this.typeFromIndex(b.button.getValue());
            this.editor.updateProfile();
        });

        this.angle = new GuiButtonElement<GuiCirculate>(mc, new GuiCirculate(0, 0, 0, 0, 0), (b) ->
        {
            this.fixture.interpolationAngle = this.typeFromIndex(b.button.getValue());
            this.editor.updateProfile();
        });

        this.pos.button.addLabel(I18n.format("aperture.gui.panels.interps.linear"));
        this.pos.button.addLabel(I18n.format("aperture.gui.panels.interps.cubic"));
        this.pos.button.addLabel(I18n.format("aperture.gui.panels.interps.hermite"));

        this.angle.button.addLabel(I18n.format("aperture.gui.panels.interps.linear"));
        this.angle.button.addLabel(I18n.format("aperture.gui.panels.interps.cubic"));
        this.angle.button.addLabel(I18n.format("aperture.gui.panels.interps.hermite"));

        this.pos.resizer().parent(this.area).set(0, 0, 0, 20).w(1, 0);
        this.angle.resizer().parent(this.area).set(0, 25, 0, 20).w(1, 0);

        this.children.add(this.pos, this.angle);
    }

    public void fill(PathFixture fixture)
    {
        this.fixture = fixture;

        this.pos.button.setValue(this.indexFromInterpType(fixture.interpolationPos));
        this.angle.button.setValue(this.indexFromInterpType(fixture.interpolationAngle));
    }

    /**
     * Get the index of interpolation type
     */
    protected int indexFromInterpType(InterpolationType type)
    {
        if (type == InterpolationType.CUBIC)
        {
            return 1;
        }

        if (type == InterpolationType.HERMITE)
        {
            return 2;
        }

        return 0;
    }

    /**
     * Get interpolation type from an index
     */
    public InterpolationType typeFromIndex(int index)
    {
        if (index == 1)
        {
            return InterpolationType.CUBIC;
        }

        if (index == 2)
        {
            return InterpolationType.HERMITE;
        }

        return InterpolationType.LINEAR;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        font.drawStringWithShadow(I18n.format("aperture.gui.panels.position"), this.pos.area.x + this.pos.area.w + 5, this.pos.area.y + 6, 0xffffff);
        font.drawStringWithShadow(I18n.format("aperture.gui.panels.angle"), this.angle.area.x + this.angle.area.w + 5, this.angle.area.y + 6, 0xffffff);
    }
}