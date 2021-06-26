package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.values.ValueKeyframeChannel;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesDopeSheetEditor;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesGraphEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import mchorse.mclib.utils.undo.CompoundUndo;
import mchorse.mclib.utils.undo.IUndo;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GuiKeyframeFixturePanel extends GuiAbstractFixturePanel<KeyframeFixture>
{
    public GuiElement buttons;

    public GuiButtonElement all;
    public GuiButtonElement x;
    public GuiButtonElement y;
    public GuiButtonElement z;
    public GuiButtonElement yaw;
    public GuiButtonElement pitch;
    public GuiButtonElement roll;
    public GuiButtonElement fov;
    public GuiButtonElement distance;

    public GuiCameraEditorKeyframesGraphEditor graph;
    public GuiCameraEditorKeyframesDopeSheetEditor dope;

    public IKey[] titles = new IKey[9];
    public int[] colors = new int[] {0xe51933, 0x19e533, 0x3319e5, 0x19cce5, 0xcc19e5, 0xe5cc19, 0xbfbfbf, 0x777777};

    private IKey title = IKey.EMPTY;
    private GuiElement current;

    public GuiKeyframeFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.buttons = new GuiElement(mc);
        this.graph = new GuiCameraEditorKeyframesGraphEditor(mc, this.editor);
        this.dope = new GuiCameraEditorKeyframesDopeSheetEditor(mc, this.editor);

        this.all = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.all"), (b) -> this.selectChannel(null, 0));
        this.x = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.x"), (b) -> this.selectChannel(this.fixture.x, 1));
        this.y = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.y"), (b) -> this.selectChannel(this.fixture.y, 2));
        this.z = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.z"), (b) -> this.selectChannel(this.fixture.z, 3));
        this.yaw = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.yaw"), (b) -> this.selectChannel(this.fixture.yaw, 4));
        this.pitch = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.pitch"), (b) -> this.selectChannel(this.fixture.pitch, 5));
        this.roll = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.roll"), (b) -> this.selectChannel(this.fixture.roll, 6));
        this.fov = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.fov"), (b) -> this.selectChannel(this.fixture.fov, 7));
        this.distance = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.distance"), (b) -> this.selectChannel(this.fixture.distance, 8));

        this.buttons.add(this.all);
        this.buttons.add(this.x, this.y, this.z);
        this.buttons.add(this.yaw, this.pitch, this.roll, this.fov);
        this.buttons.add(this.distance);

        for (int i = 0; i < this.titles.length; i++)
        {
            this.titles[i] = ((GuiButtonElement) this.buttons.getChildren().get(i)).label;
        }

        this.buttons.flex().relative(this.graph).x(10).y(-25).w(1F).h(20).row(5).resize().height(20).padding(0);
        this.graph.flex().relative(this).y(0.5F, 0).wh(1F, 0.5F);
        this.dope.flex().relative(this).y(0.5F, 0).wh(1F, 0.5F);

        this.add(this.graph, this.dope, this.buttons);

        this.keys().register(IKey.lang("aperture.gui.panels.keys.toggle_keyframes"), Keyboard.KEY_N, this::toggleKeyframes).category(CATEGORY);
    }

    private void toggleKeyframes()
    {
        if (this.current != null)
        {
            this.buttons.toggleVisible();
            this.current.toggleVisible();
        }
    }

    @Override
    public void updateDurationSettings()
    {
        super.updateDurationSettings();

        this.dope.updateConverter();
        this.graph.updateConverter();
    }

    @Override
    public void select(KeyframeFixture fixture, long duration)
    {
        boolean same = this.fixture == fixture;

        super.select(fixture, duration);

        this.graph.interpolations.setVisible(false);
        this.graph.graph.setDuration(fixture.getDuration());
        this.dope.graph.setDuration(fixture.getDuration());

        if (!same)
        {
            this.dope.setFixture(fixture);
            this.selectChannel(null, 0);
        }

        if (duration != -1)
        {
            this.graph.graph.selectByDuration(duration);
            this.dope.graph.selectByDuration(duration);
        }
    }

    @Override
    protected void updateDuration(long value)
    {
        super.updateDuration(value);
        this.graph.graph.setDuration(value);
        this.dope.graph.setDuration(value);
    }

    public void selectChannel(ValueKeyframeChannel channel, int id)
    {
        this.title = this.titles[id];
        this.dope.setVisible(id == 0);
        this.graph.setVisible(id != 0);
        this.buttons.setVisible(true);

        this.current = id == 0 ? this.dope : this.graph;

        if (channel != null)
        {
            this.graph.setChannel(channel, this.colors[id - 1]);
        }
    }

    @Override
    public void editFixture(Position position)
    {
        long tick = this.editor.timeline.value - this.currentOffset();
        
        double x = position.point.x;
        double y = position.point.y;
        double z = position.point.z;
        float yaw = position.angle.yaw;
        float pitch = position.angle.pitch;
        float distance = this.fixture.distance.get().isEmpty() ? 0F : (float)this.fixture.distance.get().interpolate(tick) + 0.05F;
        
        y -= distance * Math.sin(Math.toRadians(pitch));
        float distYaw = (float) (distance * Math.cos(Math.toRadians(pitch)));
        x -= distYaw * Math.sin(Math.toRadians(yaw));
        z += distYaw * Math.cos(Math.toRadians(yaw));
        
        CompoundUndo<CameraProfile> undo = new CompoundUndo<CameraProfile>(
            this.undoKeyframes(this.fixture.x, tick, x),
            this.undoKeyframes(this.fixture.y, tick, y),
            this.undoKeyframes(this.fixture.z, tick, z),
            this.undoKeyframes(this.fixture.yaw, tick, position.angle.yaw),
            this.undoKeyframes(this.fixture.pitch, tick, position.angle.pitch),
            this.undoKeyframes(this.fixture.roll, tick, position.angle.roll),
            this.undoKeyframes(this.fixture.fov, tick, position.angle.fov),
            this.undoKeyframes(this.fixture.distance, tick, distance - 0.05F)
        );

        this.editor.postUndo(undo, false);
    }

    private IUndo<CameraProfile> undoKeyframes(ValueKeyframeChannel channel, long tick, double value)
    {
        KeyframeChannel c = (KeyframeChannel) channel.getValue();

        c.insert(tick, value);

        IUndo<CameraProfile> undo = this.undo(channel, c);

        channel.get().insert(tick, value);

        return undo;
    }

    @Override
    public void resize()
    {
        for (IGuiElement element : this.buttons.getChildren())
        {
            GuiButtonElement button = (GuiButtonElement) element;

            button.flex().w(this.font.getStringWidth(button.label.get()) + 10);
        }

        super.resize();
    }

    @Override
    public void draw(GuiContext context)
    {
        /* Draw title of the channel */
        this.font.drawStringWithShadow(this.title.get(), this.area.ex() - this.font.getStringWidth(this.title.get()) - 10, this.graph.area.y - this.font.FONT_HEIGHT - 5, 0xffffff);

        super.draw(context);
    }
}