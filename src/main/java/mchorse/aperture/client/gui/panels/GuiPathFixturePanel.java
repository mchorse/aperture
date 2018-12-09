package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.DurablePosition;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiInterpModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule.IPointPicker;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.config.GuiCheckBox;

/**
 * Path fixture panel
 *
 * This panel has the most modules used. It's responsible for editing path
 * fixture. It uses point and angle modules to edit a position which is picked
 * from the points module. Interpolation module is used to modify path fixture's
 * interpolation methods.
 */
public class GuiPathFixturePanel extends GuiAbstractFixturePanel<PathFixture> implements IPointPicker
{
    public GuiPointModule point;
    public GuiAngleModule angle;
    public GuiPointsModule points;
    public GuiInterpModule interp;
    public GuiButtonElement<GuiCheckBox> perPointDuration;

    public DurablePosition position;

    public GuiPathFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.point = new GuiPointModule(mc, editor);
        this.angle = new GuiAngleModule(mc, editor);
        this.points = new GuiPointsModule(mc, editor, this);
        this.interp = new GuiInterpModule(mc, editor);
        this.perPointDuration = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.panels.per_point"), false, (b) ->
        {
            this.fixture.perPointDuration = b.button.isChecked();
            this.editor.updateProfile();
        });

        this.point.resizer().parent(this.area).set(0, 10, 80, 80).x(1, -80);
        this.interp.resizer().parent(this.area).set(0, 60, 80, 45);
        this.points.resizer().parent(this.area).set(140, 0, 90, 20).y(1, -20).w(1, -280);
        this.perPointDuration.resizer().parent(this.area).set(0, 110, this.perPointDuration.button.width, 11);

        this.children.add(this.point, this.angle, this.points, this.interp, this.perPointDuration);
    }

    @Override
    public void resize(int width, int height)
    {
        boolean h = this.editor.height - 60 > 200;

        this.angle.resizer().parent(this.area).set(0, 10, 80, 80).x(1, -170);

        if (h)
        {
            this.angle.resizer().x(1, -80).y(120);
        }

        super.resize(width, height);
    }

    @Override
    public void pickPoint(GuiPointsModule module, int index)
    {
        this.position = this.fixture.getPoint(index);

        this.point.fill(this.position.point);
        this.angle.fill(this.position.angle);

        if (this.fixture.perPointDuration)
        {
            this.duration.setValue(this.position.getDuration());
        }
        else
        {
            this.duration.setValue(this.fixture.getDuration());
        }

        if (this.editor.syncing)
        {
            this.editor.scrub.setValueFromScrub((int) this.currentOffset());
        }
    }

    @Override
    public void select(PathFixture fixture, long duration)
    {
        super.select(fixture, duration);

        int index = this.points.index;

        if (duration != -1)
        {
            index = (int) ((duration / (float) fixture.getDuration()) * fixture.getCount());

            DurablePosition pos = fixture.getPoint(index);

            this.position = pos;
            this.points.index = index;
        }

        this.point.fill(this.position.point);
        this.angle.fill(this.position.angle);
        this.points.fill(fixture);
        this.interp.fill(fixture);
        this.perPointDuration.button.setIsChecked(fixture.perPointDuration);

        if (fixture.perPointDuration && this.position != null)
        {
            this.duration.setValue(this.position.getDuration());
        }

        this.points.index = index;
    }

    @Override
    public long currentOffset()
    {
        long point = this.fixture.getTickForPoint(this.points.index);

        if (point == this.fixture.getDuration())
        {
            point--;
        }

        return super.currentOffset() + point;
    }

    @Override
    public void editFixture(EntityPlayer entity)
    {
        if (this.position != null)
        {
            this.position.set(entity);

            super.editFixture(entity);
        }
    }

    @Override
    protected void updateDuration(long value)
    {
        if (this.fixture.perPointDuration)
        {
            this.position.setDuration(value);
        }
        else
        {
            this.fixture.setDuration(value);
        }

        this.editor.updateValues();
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        this.editor.drawCenteredString(this.font, I18n.format("aperture.gui.panels.position"), this.point.area.x + this.point.area.w / 2, this.point.area.y - 14, 0xffffffff);
        this.editor.drawCenteredString(this.font, I18n.format("aperture.gui.panels.angle"), this.angle.area.x + this.angle.area.w / 2, this.angle.area.y - 14, 0xffffffff);
    }
}