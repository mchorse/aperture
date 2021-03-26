package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.InterpolationType;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.values.ValuePosition;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiInterpModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesGraphEditor;
import mchorse.aperture.client.gui.utils.undo.FixturePointsChangeUndo;
import mchorse.aperture.utils.undo.CompoundUndo;
import mchorse.aperture.utils.undo.IUndo;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.InterpolationRenderer;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.IInterpolation;
import mchorse.mclib.utils.keyframes.KeyframeInterpolations;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import javax.vecmath.Vector2d;

/**
 * Path fixture panel
 *
 * This panel has the most modules used. It's responsible for editing path
 * fixture. It uses point and angle modules to edit a position which is picked
 * from the points module. Interpolation module is used to modify path fixture's
 * interpolation methods.
 */
public class GuiPathFixturePanel extends GuiAbstractFixturePanel<PathFixture>
{
    public GuiPointModule point;
    public GuiAngleModule angle;
    public GuiPointsModule points;
    public GuiInterpModule interp;
    public GuiToggleElement useSpeed;
    public GuiCameraEditorKeyframesGraphEditor speed;

    public GuiElement circular;
    public GuiToggleElement autoCenter;
    public GuiTrackpadElement circularX;
    public GuiTrackpadElement circularZ;

    public ValuePosition position;

    private long update;

    public GuiPathFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.point = new GuiPointModule(mc, editor);
        this.angle = new GuiAngleModule(mc, editor);
        this.points = new GuiPointsModule(mc, editor, this::pickPoint);
        this.interp = new GuiInterpModule(mc, editor, this);
        this.useSpeed = new GuiToggleElement(mc, IKey.lang("aperture.gui.panels.use_speed_enable"), false, (b) ->
        {
            this.editor.postUndo(this.undo(this.fixture.useSpeed, b.isToggled()));

            boolean useSpeed = this.fixture.useSpeed.get();

            this.speed.setVisible(useSpeed);
            this.updateSpeedPanel();

            if (useSpeed)
            {
                this.fixture.updateSpeedCache();
            }
        });
        this.speed = new GuiCameraEditorKeyframesGraphEditor(mc, editor);

        this.autoCenter = new GuiToggleElement(mc, IKey.lang("aperture.gui.panels.auto_center"), (b) ->
        {
            IUndo<CameraProfile> undo = this.undo(this.fixture.circularAutoCenter, b.isToggled());

            if (!b.isToggled())
            {
                Vector2d center = this.fixture.calculateCenter(new Vector2d());

                this.circularX.setValue(center.x);
                this.circularZ.setValue(center.y);
                this.editor.postUndo(new CompoundUndo<CameraProfile>(
                    undo,
                    this.undo(this.fixture.circularX, center.x),
                    this.undo(this.fixture.circularZ, center.y)
                ).noMerging());
            }
            else
            {
                this.editor.postUndo(undo);
            }

            this.updateCircular();
        });

        this.circularX = new GuiTrackpadElement(mc, (value) -> this.editor.postUndo(this.undo(this.fixture.circularX, value)));
        this.circularX.tooltip(IKey.lang("aperture.gui.panels.circular_x"));
        this.circularZ = new GuiTrackpadElement(mc, (value) -> this.editor.postUndo(this.undo(this.fixture.circularZ, value)));
        this.circularZ.tooltip(IKey.lang("aperture.gui.panels.circular_z"));

        this.circular = new GuiElement(mc);
        this.circular.flex().column(5).vertical().stretch().height(20);
        this.circular.add(Elements.label(IKey.lang("aperture.gui.panels.circular")).background(0x88000000), this.autoCenter);

        this.points.flex().relative(this.left.flex()).x(1F, 40).y(1F, -30).wTo(this.right.flex(), -80).h(20);
        this.speed.flex().relative(this).y(0.55F, 0).w(1F).h(0.45F);
        this.left.flex().w(140);

        this.left.add(this.interp);
        this.left.add(Elements.label(IKey.lang("aperture.gui.panels.use_speed")).background(0x88000000), this.useSpeed);
        this.left.markContainer();
        this.right.add(this.point, this.angle);

        this.prepend(this.speed);
        this.add(this.points);

        this.keys().register(IKey.lang("aperture.gui.panels.keys.velocity"), Keyboard.KEY_E, () -> this.useSpeed.clickItself(GuiBase.getCurrent())).held(Keyboard.KEY_LSHIFT).active(editor::isFlightDisabled).category(CATEGORY);
    }

    @Override
    public void handleUndo(IUndo<CameraProfile> undo, boolean redo)
    {
        super.handleUndo(undo, redo);

        if (undo instanceof FixturePointsChangeUndo)
        {
            FixturePointsChangeUndo points = (FixturePointsChangeUndo) undo;

            this.pickPoint(redo ? points.getPoint() : points.getLastPoint());
        }
    }

    @Override
    public void updateDurationSettings()
    {
        super.updateDurationSettings();

        this.speed.updateConverter();
    }

    public void interpolationWasUpdated(boolean position)
    {
        if (position)
        {
            this.updateCircular();
        }
    }

    private void updateCircular()
    {
        this.circular.removeFromParent();
        this.circularX.removeFromParent();
        this.circularZ.removeFromParent();

        if (!this.autoCenter.isToggled())
        {
            this.circular.add(this.circularX);
            this.circular.add(this.circularZ);
        }

        if (this.fixture.interpolation.get() == InterpolationType.CIRCULAR)
        {
            this.right.add(this.circular);
        }

        this.right.resize();
    }

    private void updateSpeedPanel()
    {
        if (this.fixture.useSpeed.get())
        {
            this.left.flex().hTo(this.speed.area);
            this.right.flex().hTo(this.speed.area);
        }
        else
        {
            this.left.flex().hTo(this.area, 1F);
            this.right.flex().hTo(this.area, 1F);
        }

        this.resize();
    }

    @Override
    public void profileWasUpdated()
    {
        if (this.fixture.useSpeed.get())
        {
            this.update = System.currentTimeMillis() + 100;
        }
    }

    private ValuePosition getPosition(int index)
    {
        Value value = this.fixture.points.getSubValues().get(index);

        return value instanceof ValuePosition ? (ValuePosition) value : null;
    }

    public void pickPoint(int index)
    {
        this.points.setIndex(index);
        this.position = this.getPosition(index);

        this.point.fill(this.position.getPoint());
        this.angle.fill(this.position.getAngle());
        this.setDuration(this.fixture.getDuration());

        if (this.editor.isSyncing())
        {
            this.editor.timeline.setValueFromScrub((int) this.currentOffset());
        }
    }

    @Override
    public void select(PathFixture fixture, long duration)
    {
        super.select(fixture, duration);

        int index = this.points.index;

        if (duration != -1)
        {
            index = (int) ((duration / (float) fixture.getDuration()) * fixture.size());
        }
        else if (index >= fixture.size())
        {
            index = 0;
        }

        this.position = this.getPosition(index);
        this.points.index = index;

        this.point.fill(this.position.getPoint());
        this.angle.fill(this.position.getAngle());
        this.points.fill(fixture);
        this.interp.fill(fixture);
        this.useSpeed.toggled(fixture.useSpeed.get());
        this.updateSpeedPanel();

        this.speed.graph.setDuration(fixture.getDuration());
        this.speed.setChannel(fixture.speed, 0x0088ff);
        this.speed.setVisible(this.fixture.useSpeed.get());

        this.autoCenter.toggled(this.fixture.circularAutoCenter.get());
        this.circularX.setValue(this.fixture.circularX.get());
        this.circularZ.setValue(this.fixture.circularZ.get());
        this.updateCircular();

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
    public void editFixture(Position position)
    {
        if (this.position != null)
        {
            this.editor.postUndo(this.undo(this.position, position));

            super.editFixture(position);
        }
    }

    @Override
    protected void updateDuration(long value)
    {
        super.updateDuration(value);

        this.speed.graph.setDuration((int) value);
    }

    @Override
    public void draw(GuiContext context)
    {
        if (this.fixture.useSpeed.get() && this.update > 0 && System.currentTimeMillis() >= this.update)
        {
            this.fixture.updateSpeedCache();
            this.update = 0;
        }

        super.draw(context);
    }
}