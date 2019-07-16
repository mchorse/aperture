package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiCircularModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

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

        this.children.add(this.point, this.circular);
    }

    @Override
    public void select(CircularFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.point.fill(fixture.start);
        this.circular.fill(fixture);
    }

    @Override
    public void resize(int width, int height)
    {
        boolean h = this.resizer().getH() > 200;

        this.point.resizer().parent(this.area).set(0, 10, 80, 80).x(1, -80);
        this.circular.resizer().parent(this.area).set(0, 10, 80, 80).x(1, -170);

        if (h)
        {
            this.circular.resizer().x(1, -80).y(120);
        }

        super.resize(width, height);
    }

    @Override
    public void editFixture(Position position)
    {
        this.fixture.start.set(position.point);

        super.editFixture(position);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        this.editor.drawCenteredString(this.font, I18n.format("aperture.gui.panels.position"), this.point.area.x + this.point.area.w / 2, this.point.area.y - 14, 0xffffffff);
        this.editor.drawCenteredString(this.font, I18n.format("aperture.gui.panels.circle"), this.circular.area.x + this.circular.area.w / 2, this.circular.area.y - 14, 0xffffffff);
    }
}