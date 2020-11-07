package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.client.gui.dashboard.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiCircularModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import net.minecraft.client.Minecraft;

/**
 * Circular fixture panel
 *
 * This panel is responsible for editing a circular camera fixture using point
 * and its own circular module (which is basically positionioned like an angle
 * module, but have different set of values).
 */
public class GuiCircularFixturePanel extends GuiAbstractFixturePanel<CircularFixture>
{
    public GuiPointModule point;
    public GuiCircularModule circular;

    public GuiCircularFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.point = new GuiPointModule(mc, editor);
        this.circular = new GuiCircularModule(mc, editor);

        this.right.add(this.point, this.circular);
    }

    @Override
    public void select(CircularFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.point.fill(fixture.start);
        this.circular.fill(fixture);
    }

    @Override
    public void editFixture(Position position)
    {
        this.fixture.start.set(position.point);

        super.editFixture(position);
    }
}