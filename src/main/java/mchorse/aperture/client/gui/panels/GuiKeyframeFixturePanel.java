package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.keyframe.AllKeyframeChannel;
import mchorse.aperture.client.gui.utils.GuiFixtureKeyframesDopeSheetEditor;
import mchorse.aperture.client.gui.utils.GuiFixtureKeyframesGraphEditor;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class GuiKeyframeFixturePanel extends GuiAbstractFixturePanel<KeyframeFixture>
{
    public GuiElements<GuiButtonElement> buttons;

    public GuiButtonElement<GuiButton> all;
    public GuiButtonElement<GuiButton> x;
    public GuiButtonElement<GuiButton> y;
    public GuiButtonElement<GuiButton> z;
    public GuiButtonElement<GuiButton> yaw;
    public GuiButtonElement<GuiButton> pitch;
    public GuiButtonElement<GuiButton> roll;
    public GuiButtonElement<GuiButton> fov;

    public GuiFixtureKeyframesGraphEditor<GuiKeyframeFixturePanel> graph;
    public GuiFixtureKeyframesDopeSheetEditor dope;

    public AllKeyframeChannel allChannel = new AllKeyframeChannel();
    public String[] titles = new String[8];
    public int[] colors = new int[] {0xff1392, 0xe51933, 0x19e533, 0x3319e5, 0x19cce5, 0xcc19e5, 0xe5cc19, 0xbfbfbf};

    private String title = "";

    public GuiKeyframeFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.buttons = new GuiElements<>();
        this.graph = new GuiFixtureKeyframesGraphEditor<GuiKeyframeFixturePanel>(mc, this);
        this.dope = new GuiFixtureKeyframesDopeSheetEditor(mc, this);

        this.all = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.all"), (b) -> this.selectChannel(this.allChannel));
        this.x = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.x"), (b) -> this.selectChannel(this.fixture.x));
        this.y = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.y"), (b) -> this.selectChannel(this.fixture.y));
        this.z = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.z"), (b) -> this.selectChannel(this.fixture.z));
        this.yaw = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.yaw"), (b) -> this.selectChannel(this.fixture.yaw));
        this.pitch = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.pitch"), (b) -> this.selectChannel(this.fixture.pitch));
        this.roll = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.roll"), (b) -> this.selectChannel(this.fixture.roll));
        this.fov = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.fov"), (b) -> this.selectChannel(this.fixture.fov));

        this.buttons.add(this.all);
        this.buttons.add(this.x);
        this.buttons.add(this.y);
        this.buttons.add(this.z);
        this.buttons.add(this.yaw);
        this.buttons.add(this.pitch);
        this.buttons.add(this.roll);
        this.buttons.add(this.fov);

        for (int i = 0; i < this.titles.length; i++)
        {
            this.titles[i] = this.buttons.elements.get(i).button.displayString;
        }

        int i = 0;
        int x = 0;

        for (GuiButtonElement button : this.buttons.elements)
        {
            if (i > 7) break;

            button.resizer().parent(this.area).set(x, 0, this.font.getStringWidth(button.button.displayString) + 15, 20).y(1, -30);

            x += button.resizer().getW() + 5;
            i++;
        }

        this.graph.resizer().parent(this.area).set(-10, 0, 0, 0).y(0.5F, 0).w(1, 20).h(0.5F, 0);
        this.dope.resizer().parent(this.area).set(-10, 0, 0, 0).y(0.5F, 0).w(1, 20).h(0.5F, -40);

        this.children.add(this.graph, this.dope, this.buttons);
    }

    @Override
    public void select(KeyframeFixture fixture, long duration)
    {
        boolean same = this.fixture == fixture;

        super.select(fixture, duration);

        this.graph.interpolations.setVisible(false);
        this.graph.graph.setDuration(fixture.getDuration());
        this.dope.graph.setDuration(fixture.getDuration());
        this.allChannel.setFixture(fixture);

        if (!same)
        {
            this.dope.setFixture(fixture);
            this.selectChannel(this.allChannel);
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

        if (channel != this.allChannel)
        {
            this.graph.graph.color = this.colors[id];
            this.graph.setChannel(channel);
        }
        else
        {
            this.allChannel.setFixture(this.fixture);
        }
    }

    @Override
    public void editFixture(EntityPlayer entity)
    {
        Position pos = new Position(entity);
        long tick = this.editor.scrub.value - this.currentOffset();

        this.fixture.x.insert(tick, (float) pos.point.x);
        this.fixture.y.insert(tick, (float) pos.point.y);
        this.fixture.z.insert(tick, (float) pos.point.z);
        this.fixture.yaw.insert(tick, pos.angle.yaw);
        this.fixture.pitch.insert(tick, pos.angle.pitch);
        this.fixture.roll.insert(tick, pos.angle.roll);
        this.fixture.fov.insert(tick, pos.angle.fov);
        this.allChannel.setFixture(this.fixture);

        this.editor.updateProfile();
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        /* Draw title of the channel */
        this.editor.drawCenteredString(this.font, this.title, this.area.getX(0.5F), this.graph.area.y - this.font.FONT_HEIGHT - 5, 0xffffff);

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }
}