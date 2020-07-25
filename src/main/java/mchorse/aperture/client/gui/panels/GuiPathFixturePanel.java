package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiInterpModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule.IPointPicker;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesGraphEditor;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.keyframes.KeyframeEasing;
import mchorse.mclib.utils.keyframes.KeyframeInterpolation;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

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
    public GuiToggleElement useSpeed;
    public GuiButtonElement toKeyframe;
    public GuiCameraEditorKeyframesGraphEditor speed;

    public Position position;

    private long update;

    public GuiPathFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.point = new GuiPointModule(mc, editor);
        this.angle = new GuiAngleModule(mc, editor);
        this.points = new GuiPointsModule(mc, editor, this);
        this.interp = new GuiInterpModule(mc, editor);
        this.useSpeed = new GuiToggleElement(mc, IKey.lang("aperture.gui.panels.use_speed"), false, (b) ->
        {
            this.fixture.useSpeed = b.isToggled();
            this.speed.setVisible(this.fixture.useSpeed);
            this.editor.updateProfile();
            this.updateSpeedPanel();

            if (this.fixture.useSpeed)
            {
                this.fixture.updateSpeedCache();
            }
        });
        this.toKeyframe = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.to_keyframe"), (b) -> this.toKeyframe());
        this.speed = new GuiCameraEditorKeyframesGraphEditor(mc, editor);
        this.speed.graph.setColor(0x0088ff);

        this.points.flex().relative(this.left.flex()).x(1F, 40).y(1F, -30).wTo(this.right.flex(), -80).h(20);
        this.speed.flex().relative(this).y(0.55F, 0).w(1F).h(0.45F);
        this.left.flex().w(140);

        this.left.add(this.interp, this.useSpeed, this.toKeyframe);
        this.left.markContainer();
        this.right.add(this.point, this.angle);

        this.prepend(this.speed);
        this.add(this.points);

        this.keys().register(IKey.lang("aperture.gui.panels.keys.velocity"), Keyboard.KEY_E, () -> this.useSpeed.clickItself(GuiBase.getCurrent())).held(Keyboard.KEY_LSHIFT).active(editor::isFlightMode).category(CATEGORY);
    }

    private void updateSpeedPanel()
    {
        if (this.fixture.useSpeed)
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
		if (this.fixture.useSpeed)
		{
			this.update = System.currentTimeMillis() + 100;
		}
	}

	private void toKeyframe()
    {
        int c = this.fixture.getCount();

        if (c <= 1)
        {
            return;
        }

        long duration = this.fixture.getDuration();
        KeyframeFixture fixture = new KeyframeFixture(duration);
        AbstractFixture.copyModifiers(this.fixture, fixture);
        KeyframeInterpolation pos = this.fixture.interpolationPos.interp;
        KeyframeInterpolation angle = this.fixture.interpolationAngle.interp;
        KeyframeEasing posEasing = this.fixture.interpolationPos.easing;
        KeyframeEasing angleEasing = this.fixture.interpolationAngle.easing;

        long x;
        int i = 0;

        for (Position point : this.fixture.getPoints())
        {
            x = (int) (i / (c - 1F) * duration);

            int index = fixture.x.insert(x, (float) point.point.x);
            fixture.y.insert(x, (float) point.point.y);
            fixture.z.insert(x, (float) point.point.z);
            fixture.yaw.insert(x, point.angle.yaw);
            fixture.pitch.insert(x, point.angle.pitch);
            fixture.roll.insert(x, point.angle.roll);
            fixture.fov.insert(x, point.angle.fov);

            fixture.x.get(index).setInterpolation(pos, posEasing);
            fixture.y.get(index).setInterpolation(pos, posEasing);
            fixture.z.get(index).setInterpolation(pos, posEasing);
            fixture.yaw.get(index).setInterpolation(angle, angleEasing);
            fixture.pitch.get(index).setInterpolation(angle, angleEasing);
            fixture.roll.get(index).setInterpolation(angle, angleEasing);
            fixture.fov.get(index).setInterpolation(angle, angleEasing);

            i ++;
        }

        this.editor.createFixture(fixture);
    }

    @Override
    public void pickPoint(GuiPointsModule module, int index)
    {
        this.position = this.fixture.getPoint(index);

        this.point.fill(this.position.point);
        this.angle.fill(this.position.angle);
        this.duration.setValue(this.fixture.getDuration());

        if (this.editor.isSyncing())
        {
            this.editor.timeline.setValueFromScrub((int) this.currentOffset());
        }
    }

    @Override
    public void select(PathFixture fixture, long duration)
    {
        boolean same = this.fixture == fixture;

        super.select(fixture, duration);

        int index = this.points.index;

        if (duration != -1)
        {
            index = (int) ((duration / (float) fixture.getDuration()) * fixture.getCount());

            Position pos = fixture.getPoint(index);

            this.position = pos;
            this.points.index = index;
        }

        this.point.fill(this.position.point);
        this.angle.fill(this.position.angle);
        this.points.fill(fixture);
        this.interp.fill(fixture);
        this.useSpeed.toggled(fixture.useSpeed);
        this.updateSpeedPanel();

        if (!same)
        {
            this.speed.graph.setDuration(fixture.getDuration());
            this.speed.setChannel(fixture.speed);
            this.speed.setVisible(this.fixture.useSpeed);
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
    public void editFixture(Position position)
    {
        if (this.position != null)
        {
            this.position.set(position);

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
    	if (this.fixture.useSpeed && this.update > 0 && System.currentTimeMillis() >= this.update)
	    {
	    	this.fixture.updateSpeedCache();
	    	this.update = 0;
	    }

        super.draw(context);
    }
}