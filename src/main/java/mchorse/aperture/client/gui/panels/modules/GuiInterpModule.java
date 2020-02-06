package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.GuiInterpolationTypeList;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

/**
 * Path fixture interpolations GUI module
 *
 * This module is responsible for changing angle and/or position interpolation
 * of the path fixture.
 */
public class GuiInterpModule extends GuiAbstractModule
{
    public GuiButtonElement<GuiButton> pos;
    public GuiButtonElement<GuiButton> angle;
    public GuiInterpolationTypeList interps;

    public PathFixture fixture;
    public boolean pickPos = false;

    public GuiInterpModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.pos = GuiButtonElement.button(mc, "", (b) ->
        {
            if (this.interps.isVisible())
            {
                this.interps.setVisible(false);
            }
            else
            {
                this.pickPos = true;
                this.interps.setCurrent(this.fixture.interpolationPos);
                this.interps.setVisible(true);

                this.interps.resizer().relative(this.pos.resizer());
                this.interps.resize(0, 0);
            }
        });

        this.angle = GuiButtonElement.button(mc, "", (b) ->
        {
            if (this.interps.isVisible())
            {
                this.interps.setVisible(false);
            }
            else
            {
                this.pickPos = false;
                this.interps.setCurrent(this.fixture.interpolationAngle);
                this.interps.setVisible(true);

                this.interps.resizer().relative(this.angle.resizer());
                this.interps.resize(0, 0);
            }
        });

        this.interps = new GuiInterpolationTypeList(mc, (interp) ->
        {
            if (this.pickPos)
            {
                this.fixture.interpolationPos = interp;
                this.pos.button.displayString = I18n.format("aperture.gui.panels.interps." + interp.name);
            }
            else
            {
                this.fixture.interpolationAngle = interp;
                this.angle.button.displayString = I18n.format("aperture.gui.panels.interps." + interp.name);
            }

            this.interps.setVisible(false);
            this.editor.updateProfile();
        });

        this.pos.resizer().parent(this.area).set(0, 0, 0, 20).w(1, 0);
        this.angle.resizer().parent(this.area).set(0, 25, 0, 20).w(1, 0);
        this.interps.resizer().y(20).w(1, 0).h(96);

        this.children.add(this.pos, this.angle, this.interps);
    }

    public void fill(PathFixture fixture)
    {
        this.fixture = fixture;

        this.interps.setVisible(false);
        this.interps.setCurrent(fixture.interpolationPos);
        this.pos.button.displayString = I18n.format("aperture.gui.panels.interps." + this.interps.getCurrent().name);
        this.interps.setCurrent(fixture.interpolationAngle);
        this.angle.button.displayString = I18n.format("aperture.gui.panels.interps." + this.interps.getCurrent().name);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        this.font.drawStringWithShadow(I18n.format("aperture.gui.panels.position"), this.pos.area.x + this.pos.area.w + 5, this.pos.area.y + 6, 0xffffff);
        this.font.drawStringWithShadow(I18n.format("aperture.gui.panels.angle"), this.angle.area.x + this.angle.area.w + 5, this.angle.area.y + 6, 0xffffff);
    }
}