package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.keyframe.AllKeyframeChannel;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesDopeSheetEditor;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesGraphEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;

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

    public GuiCameraEditorKeyframesGraphEditor graph;
    public GuiCameraEditorKeyframesDopeSheetEditor dope;

    public IKey[] titles = new IKey[8];
    public int[] colors = new int[] {0xe51933, 0x19e533, 0x3319e5, 0x19cce5, 0xcc19e5, 0xe5cc19, 0xbfbfbf};

    private IKey title = IKey.EMPTY;

    public GuiKeyframeFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.buttons = new GuiElement(mc);
        this.graph = new GuiCameraEditorKeyframesGraphEditor(mc, this.editor);
        this.dope = new GuiCameraEditorKeyframesDopeSheetEditor(mc, this.editor);

        this.all = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.all"), (b) -> this.selectChannel(null));
        this.x = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.x"), (b) -> this.selectChannel(this.fixture.x));
        this.y = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.y"), (b) -> this.selectChannel(this.fixture.y));
        this.z = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.z"), (b) -> this.selectChannel(this.fixture.z));
        this.yaw = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.yaw"), (b) -> this.selectChannel(this.fixture.yaw));
        this.pitch = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.pitch"), (b) -> this.selectChannel(this.fixture.pitch));
        this.roll = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.roll"), (b) -> this.selectChannel(this.fixture.roll));
        this.fov = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.fov"), (b) -> this.selectChannel(this.fixture.fov));

        this.buttons.add(this.all);
        this.buttons.add(this.x, this.y, this.z);
        this.buttons.add(this.yaw, this.pitch, this.roll, this.fov);

        for (int i = 0; i < this.titles.length; i++)
        {
            this.titles[i] = ((GuiButtonElement) this.buttons.getChildren().get(i)).label;
        }

        this.buttons.flex().relative(this.graph).x(10).y(-25).w(1F).h(20).row(5).resize().height(20).padding(0);
        this.graph.flex().relative(this).y(0.5F, 0).wh(1F, 0.5F);
        this.dope.flex().relative(this).y(0.5F, 0).wh(1F, 0.5F);

        this.add(this.graph, this.dope, this.buttons);
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
            this.selectChannel(null);
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

    public void selectChannel(KeyframeChannel channel)
    {
        int id = 0;

        if (channel == this.fixture.x) id = 1;
        else if (channel == this.fixture.y) id = 2;
        else if (channel == this.fixture.z) id = 3;
        else if (channel == this.fixture.yaw) id = 4;
        else if (channel == this.fixture.pitch) id = 5;
        else if (channel == this.fixture.roll) id = 6;
        else if (channel == this.fixture.fov) id = 7;

        this.title = this.titles[id];
        this.dope.setVisible(id == 0);
        this.graph.setVisible(id != 0);

        if (channel != null)
        {
            this.graph.setChannel(channel, this.colors[id - 1]);
        }
    }

    @Override
    public void editFixture(Position position)
    {
        long tick = this.editor.timeline.value - this.currentOffset();

        this.fixture.x.insert(tick, (float) position.point.x);
        this.fixture.y.insert(tick, (float) position.point.y);
        this.fixture.z.insert(tick, (float) position.point.z);
        this.fixture.yaw.insert(tick, position.angle.yaw);
        this.fixture.pitch.insert(tick, position.angle.pitch);
        this.fixture.roll.insert(tick, position.angle.roll);
        this.fixture.fov.insert(tick, position.angle.fov);

        this.editor.updateProfile();
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