package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Idle fixture panel
 *
 * This panel is responsible for editing an idle fixture. This panel uses basic
 * point and angle modules for manipulating idle fixture's position.
 */
public class GuiIdleFixturePanel extends GuiAbstractFixturePanel<IdleFixture>
{
    public GuiPointModule point;
    public GuiAngleModule angle;

    public GuiIdleFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.point = new GuiPointModule(mc, editor);
        this.angle = new GuiAngleModule(mc, editor);

        this.add(this.point, this.angle);
    }

    @Override
    public void select(IdleFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.point.fill(fixture.position.point);
        this.angle.fill(fixture.position.angle);
    }

    @Override
    public void resize()
    {
        boolean h = this.flex().getH() > 200;

        this.point.flex().relative(this).set(0, 10, 80, 80).x(1, -80);
        this.angle.flex().relative(this).set(0, 10, 80, 80).x(1, -170);

        if (h)
        {
            this.angle.flex().x(1, -80).y(120);
        }

        super.resize();
    }

    @Override
    public void editFixture(Position position)
    {
        this.fixture.position.set(position);

        super.editFixture(position);
    }

    @Override
    public void draw(GuiContext context)
    {
        super.draw(context);

        this.drawCenteredString(this.font, I18n.format("aperture.gui.panels.position"), this.point.area.x + this.point.area.w / 2, this.point.area.y - 14, 0xffffffff);
        this.drawCenteredString(this.font, I18n.format("aperture.gui.panels.angle"), this.angle.area.x + this.angle.area.w / 2, this.angle.area.y - 14, 0xffffffff);
    }
}