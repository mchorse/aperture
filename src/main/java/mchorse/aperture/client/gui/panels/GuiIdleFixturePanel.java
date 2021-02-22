package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import net.minecraft.client.Minecraft;

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

        this.right.add(this.point, this.angle);
    }

    @Override
    public void select(IdleFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.point.fill(fixture.position.getPoint());
        this.angle.fill(fixture.position.getAngle());
    }

    @Override
    public void editFixture(Position position)
    {
        this.editor.postUndo(this.undo("position", position));

        super.editFixture(position);
    }
}