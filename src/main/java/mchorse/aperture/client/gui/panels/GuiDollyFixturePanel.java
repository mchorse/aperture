package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.DollyFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import mchorse.aperture.utils.undo.CompoundUndo;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiInterpolationList;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.tooltips.InterpolationTooltip;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.InterpolationRenderer;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiDollyFixturePanel extends GuiAbstractFixturePanel<DollyFixture>
{
    public GuiPointModule point;
    public GuiAngleModule angle;

    public GuiTrackpadElement distance;
    public GuiIconElement reverse;
    public GuiButtonElement pickInterp;
    public GuiInterpolationList interps;

    public GuiTrackpadElement yaw;
    public GuiTrackpadElement pitch;

    public GuiDollyFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.point = new GuiPointModule(mc, editor);
        this.angle = new GuiAngleModule(mc, editor);
        this.distance = new GuiTrackpadElement(mc, (value) -> this.editor.postUndo(this.undo(this.fixture.distance, value.floatValue())));
        this.distance.tooltip(IKey.lang("aperture.gui.panels.dolly.distance"));
        this.reverse = new GuiIconElement(mc, Icons.REVERSE, (b) -> this.reverse());
        this.reverse.tooltip(IKey.lang("aperture.gui.panels.dolly.reverse"));
        this.yaw = new GuiTrackpadElement(mc, (value) -> this.editor.postUndo(this.undo(this.fixture.yaw, value.floatValue())));
        this.yaw.tooltip(IKey.lang("aperture.gui.panels.dolly.yaw"));
        this.pitch = new GuiTrackpadElement(mc, (value) -> this.editor.postUndo(this.undo(this.fixture.pitch, value.floatValue())));
        this.pitch.tooltip(IKey.lang("aperture.gui.panels.dolly.pitch"));

        InterpolationTooltip tooltip = new InterpolationTooltip(1F, 0F, () -> this.fixture.interp.get(), null);

        this.pickInterp = new GuiButtonElement(mc, IKey.lang(""), (b) ->
        {
            if (this.interps.hasParent())
            {
                this.interps.removeFromParent();
            }
            else
            {
                this.interps.setCurrent(this.fixture.interp.get());

                this.add(this.interps);
                this.interps.flex().relative(this.pickInterp);
                this.interps.resize();
            }
        });
        this.pickInterp.tooltip(tooltip);

        this.interps = new GuiInterpolationList(mc, (interp) ->
        {
            this.editor.postUndo(this.undo(this.fixture.interp, interp.get(0)));
            this.pickInterp.label.set(interp.get(0).getKey());
            this.editor.updateProfile();
        });
        this.interps.tooltip(tooltip).markIgnored().flex().y(1F).w(1F).h(96);

        this.right.add(this.point, this.angle);
        this.left.add(Elements.label(IKey.lang("aperture.gui.fixtures.dolly")).background(), Elements.row(mc, 0, 0, 20, this.distance, this.reverse), this.yaw, this.pitch, this.pickInterp);
    }

    private void reverse()
    {
        Position position = new Position();

        this.fixture.applyLast(this.editor.getProfile(), position);

        this.editor.postUndo(new CompoundUndo<CameraProfile>(
            this.undo(this.fixture.position, position),
            this.undo(this.fixture.distance, -this.fixture.distance.get())
        ));

        this.select(this.fixture, 0);
    }

    @Override
    public void select(DollyFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.point.fill(fixture.position.getPoint());
        this.angle.fill(fixture.position.getAngle());

        this.yaw.setValue(fixture.yaw.get());
        this.pitch.setValue(fixture.pitch.get());
        this.distance.setValue(fixture.distance.get());

        this.interps.removeFromParent();
        this.interps.setCurrent(fixture.interp.get());
        this.pickInterp.label.set(fixture.interp.get().getKey());
    }

    @Override
    public void editFixture(Position position)
    {
        this.editor.postUndo(new CompoundUndo<CameraProfile>(
            this.undo(this.fixture.position, position),
            this.undo(this.fixture.yaw, position.angle.yaw),
            this.undo(this.fixture.pitch, position.angle.pitch)
        ));

        super.editFixture(position);
    }

    @Override
    public void draw(GuiContext context)
    {
        double speed = this.fixture.distance.get() / (this.fixture.getDuration() / 20D);
        String label = I18n.format("aperture.gui.panels.dolly.speed", GuiTrackpadElement.FORMAT.format(speed));

        GuiDraw.drawTextBackground(this.font, label, this.area.mx(this.font.getStringWidth(label)), this.area.ey() - this.font.FONT_HEIGHT - 20, 0xffffff, ColorUtils.HALF_BLACK);

        super.draw(context);
    }
}