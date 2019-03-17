package mchorse.aperture.client.gui.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Interpolation;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.GuiGraphElement;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.widgets.buttons.GuiCirculate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class GuiKeyframeFixturePanel extends GuiAbstractFixturePanel<KeyframeFixture>
{
    public GuiTrackpadElement tick;
    public GuiTrackpadElement value;

    public GuiElements<GuiButtonElement> buttons;
    public GuiElements<GuiElement> frameButtons;

    public GuiButtonElement<GuiButton> all;
    public GuiButtonElement<GuiButton> x;
    public GuiButtonElement<GuiButton> y;
    public GuiButtonElement<GuiButton> z;
    public GuiButtonElement<GuiButton> yaw;
    public GuiButtonElement<GuiButton> pitch;
    public GuiButtonElement<GuiButton> roll;
    public GuiButtonElement<GuiButton> fov;

    public GuiButtonElement<GuiButton> add;
    public GuiButtonElement<GuiButton> remove;
    public GuiButtonElement<GuiButton> interp;
    public GuiListElement<Interpolation> interpolations;
    public GuiButtonElement<GuiCirculate> easing;
    public GuiGraphElement graph;

    private AllKeyframeChannel allChannel = new AllKeyframeChannel();
    private String[] titles = new String[8];
    private String title = "";
    private int[] colors = new int[] {0xff1392, 0xe51933, 0x19e533, 0x3319e5, 0x19cce5, 0xcc19e5, 0xe5cc19, 0xbfbfbf};

    public GuiKeyframeFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.tick = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.tick"), (value) ->
        {
            this.graph.getCurrent().setTick(value.longValue());
            this.graph.sliding = true;
        });
        this.tick.setLimit(0, Integer.MAX_VALUE, true);
        this.value = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.value"), (value) ->
        {
            this.graph.getCurrent().setValue(value);
        });

        this.buttons = new GuiElements<>();
        this.frameButtons = new GuiElements<>();

        this.all = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.all"), (b) -> this.selectChannel(this.allChannel));
        this.x = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.x"), (b) -> this.selectChannel(this.fixture.x));
        this.y = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.y"), (b) -> this.selectChannel(this.fixture.y));
        this.z = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.z"), (b) -> this.selectChannel(this.fixture.z));
        this.yaw = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.yaw"), (b) -> this.selectChannel(this.fixture.yaw));
        this.pitch = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.pitch"), (b) -> this.selectChannel(this.fixture.pitch));
        this.roll = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.roll"), (b) -> this.selectChannel(this.fixture.roll));
        this.fov = GuiButtonElement.button(mc, I18n.format("aperture.gui.panels.fov"), (b) -> this.selectChannel(this.fixture.fov));

        this.add = GuiButtonElement.button(mc, I18n.format("aperture.gui.add"), (b) -> this.addKeyframe());
        this.remove = GuiButtonElement.button(mc, I18n.format("aperture.gui.remove"), (b) -> this.removeKeyframe());
        this.interp = GuiButtonElement.button(mc, "", (b) -> this.interpolations.toggleVisible());
        this.interpolations = new GuiInterpolationsList(mc, (interp) ->
        {
            this.graph.getCurrent().setInterpolation(interp);
            this.interp.button.displayString = I18n.format("aperture.gui.panels.interps." + interp.key);
            this.interpolations.setVisible(false);
            this.editor.updateProfile();
        });

        for (Interpolation interp : Interpolation.values())
        {
            this.interpolations.add(interp);
        }

        this.easing = new GuiButtonElement<GuiCirculate>(mc, new GuiCirculate(0, 0, 0, 80, 20), (b) -> this.changeEasing());
        this.easing.button.addLabel(I18n.format("aperture.gui.panels.easing.in"));
        this.easing.button.addLabel(I18n.format("aperture.gui.panels.easing.out"));
        this.easing.button.addLabel(I18n.format("aperture.gui.panels.easing.inout"));

        this.graph = new GuiGraphElement(mc, (frame) -> this.fillData(frame));
        this.graph.parent = this;

        this.buttons.add(this.all);
        this.buttons.add(this.x);
        this.buttons.add(this.y);
        this.buttons.add(this.z);
        this.buttons.add(this.yaw);
        this.buttons.add(this.pitch);
        this.buttons.add(this.roll);
        this.buttons.add(this.fov);

        this.buttons.add(this.add);
        this.buttons.add(this.remove);

        this.frameButtons.add(this.interp, this.easing, this.tick, this.value, this.interpolations);

        for (int i = 0; i < this.titles.length; i++)
        {
            this.titles[i] = this.buttons.elements.get(i).button.displayString;
        }

        this.tick.resizer().parent(this.area).set(0, 10, 80, 20).x(1, -80);
        this.value.resizer().parent(this.area).set(0, 35, 80, 20).x(1, -80);

        int i = 0;
        int x = 0;

        for (GuiButtonElement button : this.buttons.elements)
        {
            if (i > 7) break;

            button.resizer().parent(this.area).set(x, 0, this.font.getStringWidth(button.button.displayString) + 15, 20).y(1, -20);

            x += button.resizer().getW() + 5;
            i++;
        }

        this.add.resizer().parent(this.area).set(0, 0, 30, 20).y(1, -20).x(1, -85);
        this.remove.resizer().parent(this.area).set(0, 0, 50, 20).y(1, -20).x(1, -50);
        this.interp.resizer().relative(this.tick.resizer()).set(-90, 0, 80, 20);
        this.easing.resizer().relative(this.value.resizer()).set(-90, 0, 80, 20);
        this.interpolations.resizer().relative(this.interp.resizer()).set(0, 20, 80, 16 * 5);
        this.graph.resizer().parent(this.area).set(-10, 0, 0, 0).y(0.5F, 0).w(1, 20).h(0.5F, -30);

        this.children.add(this.graph, this.buttons, this.frameButtons);
    }

    private void addKeyframe()
    {
        Position pos = new Position(Minecraft.getMinecraft().thePlayer);
        float value = 0;

        if (this.graph.channel == this.fixture.x) value = pos.point.x;
        if (this.graph.channel == this.fixture.y) value = pos.point.y;
        if (this.graph.channel == this.fixture.z) value = pos.point.z;
        if (this.graph.channel == this.fixture.yaw) value = pos.angle.yaw;
        if (this.graph.channel == this.fixture.pitch) value = pos.angle.pitch;
        if (this.graph.channel == this.fixture.roll) value = pos.angle.roll;
        if (this.graph.channel == this.fixture.fov) value = pos.angle.fov;

        Easing easing = Easing.IN;
        Interpolation interp = Interpolation.LINEAR;
        Keyframe frame = this.graph.getCurrent();
        long tick = this.editor.scrub.value - this.currentOffset();
        long oldTick = tick;

        if (frame != null)
        {
            easing = frame.easing;
            interp = frame.interp;
            oldTick = frame.tick;
        }

        this.graph.selected = this.graph.channel.insert(tick, value);

        if (this.graph.channel == this.allChannel)
        {
            AllKeyframe allStar = (AllKeyframe) this.graph.getCurrent();

            for (KeyframeChannel channel : this.fixture.channels)
            {
                if (channel == this.fixture.x) value = pos.point.x;
                if (channel == this.fixture.y) value = pos.point.y;
                if (channel == this.fixture.z) value = pos.point.z;
                if (channel == this.fixture.yaw) value = pos.angle.yaw;
                if (channel == this.fixture.pitch) value = pos.angle.pitch;
                if (channel == this.fixture.roll) value = pos.angle.roll;
                if (channel == this.fixture.fov) value = pos.angle.fov;

                int index = channel.insert(tick, value);

                allStar.keyframes.add(new KeyframeCell(channel.getKeyframes().get(index), channel));
            }
        }

        if (oldTick != tick)
        {
            frame = this.graph.getCurrent();
            frame.setEasing(easing);
            frame.setInterpolation(interp);
        }

        this.fillData(frame);
    }

    private void removeKeyframe()
    {
        Keyframe frame = this.graph.getCurrent();

        if (frame == null)
        {
            return;
        }

        this.graph.channel.remove(this.graph.selected);
        this.graph.selected -= 1;
        this.editor.updateProfile();
        this.fillData(this.graph.getCurrent());
    }

    private void changeEasing()
    {
        Keyframe selected = this.graph.getCurrent();

        if (selected != null)
        {
            selected.setEasing(Easing.values()[this.easing.button.getValue()]);
            this.editor.updateProfile();
        }
    }

    @Override
    public void select(KeyframeFixture fixture, long duration)
    {
        boolean same = this.fixture == fixture;

        super.select(fixture, duration);

        this.allChannel.setFixture(fixture);
        this.interpolations.setVisible(false);
        this.graph.duration = (int) fixture.getDuration();

        if (!same)
        {
            this.selectChannel(fixture.x);
            this.graph.resetView();
        }

        if (duration != -1)
        {
            this.graph.selectByDuration(duration);
        }
    }

    @Override
    protected void updateDuration(long value)
    {
        super.updateDuration(value);
        this.graph.duration = (int) value;
    }

    public void fillData(Keyframe frame)
    {
        this.frameButtons.setVisible(frame != null);

        if (frame == null)
        {
            return;
        }

        this.tick.setValue(frame.tick);
        this.value.setValue(frame.value);
        this.interp.button.displayString = I18n.format("aperture.gui.panels.interps." + frame.interp.key);
        this.interpolations.setCurrent(frame.interp);
        this.easing.button.setValue(frame.easing.ordinal());
    }

    public void selectChannel(KeyframeChannel channel)
    {
        this.graph.channel = channel;
        int id = 0;

        if (channel == this.fixture.x) id = 1;
        else if (channel == this.fixture.y) id = 2;
        else if (channel == this.fixture.z) id = 3;
        else if (channel == this.fixture.yaw) id = 4;
        else if (channel == this.fixture.pitch) id = 5;
        else if (channel == this.fixture.roll) id = 6;
        else if (channel == this.fixture.fov) id = 7;

        if (channel == this.allChannel) this.allChannel.setFixture(this.fixture);

        this.title = this.titles[id];
        this.graph.color = this.colors[id];
        this.graph.resetView();
        this.frameButtons.setVisible(false);
    }

    @Override
    public void editFixture(EntityPlayer entity)
    {
        Position pos = new Position(entity);
        long tick = this.editor.scrub.value - this.currentOffset();

        this.fixture.x.insert(tick, pos.point.x);
        this.fixture.y.insert(tick, pos.point.y);
        this.fixture.z.insert(tick, pos.point.z);
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

    /**
     * Interpolations 
     */
    public static class GuiInterpolationsList extends GuiListElement<Interpolation>
    {
        public GuiInterpolationsList(Minecraft mc, Consumer<Interpolation> callback)
        {
            super(mc, callback);

            this.scroll.scrollItemSize = 16;
        }

        @Override
        public void sort()
        {
            Collections.sort(this.list, new Comparator<Interpolation>()
            {
                @Override
                public int compare(Interpolation o1, Interpolation o2)
                {
                    return o1.key.compareTo(o2.key);
                }
            });

            this.update();
        }

        @Override
        public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
        {
            this.scroll.draw(0x88000000);

            super.draw(tooltip, mouseX, mouseY, partialTicks);
        }

        @Override
        public void drawElement(Interpolation element, int i, int x, int y, boolean hover)
        {
            if (this.current == i)
            {
                Gui.drawRect(x, y, x + this.scroll.w, y + this.scroll.scrollItemSize, 0x880088ff);
            }

            String label = I18n.format("aperture.gui.panels.interps." + element.key);

            this.font.drawStringWithShadow(label, x + 4, y + 4, hover ? 16777120 : 0xffffff);
        }
    }

    /* All channel abstraction classes
     * 
     * Those classes allow to imitate behavior of keyframe channels 
     * while also be able to modify individual keyframes within those 
     * channels for every keyframe at specific time, the all keyframe 
     * channel will create a fake keyframe which will keep the reference
     * to the original keyframes at same timestamp */

    public static class AllKeyframeChannel extends KeyframeChannel
    {
        public KeyframeFixture fixture;

        @Override
        protected Keyframe create(long tick, float value)
        {
            return new AllKeyframe(tick);
        }

        public void setFixture(KeyframeFixture fixture)
        {
            this.fixture = fixture;

            this.keyframes.clear();

            for (KeyframeChannel channel : fixture.channels)
            {
                for (Keyframe kf : channel.getKeyframes())
                {
                    int index = this.insert(kf.tick, 0);
                    AllKeyframe allStar = (AllKeyframe) this.keyframes.get(index);

                    allStar.keyframes.add(new KeyframeCell(kf, channel));
                    allStar.easing = kf.easing;
                    allStar.interp = kf.interp;
                }
            }
        }

        @Override
        public void sort()
        {
            super.sort();

            for (KeyframeChannel channel : this.fixture.channels)
            {
                channel.sort();
            }
        }

        @Override
        public void remove(int index)
        {
            AllKeyframe kf = (AllKeyframe) this.keyframes.remove(index);

            for (KeyframeCell cell : kf.keyframes)
            {
                cell.channel.remove(cell.channel.getKeyframes().indexOf(cell.keyframe));
            }
        }
    }

    /**
     * All channel keyframe
     * 
     * This keyframe is responsible for delegating methods to actual 
     * keyframe
     */
    public static class AllKeyframe extends Keyframe
    {
        public List<KeyframeCell> keyframes = new ArrayList<KeyframeCell>();

        public AllKeyframe(long tick)
        {
            super(tick, 0);
        }

        @Override
        public void setTick(long tick)
        {
            super.tick = tick;

            for (KeyframeCell cell : this.keyframes)
            {
                cell.keyframe.setTick(tick);
            }
        }

        /* Nope */
        @Override
        public void setValue(float value)
        {}

        @Override
        public void setEasing(Easing easing)
        {
            super.setEasing(easing);

            for (KeyframeCell cell : this.keyframes)
            {
                cell.keyframe.setEasing(easing);
            }
        }

        @Override
        public void setInterpolation(Interpolation interp)
        {
            super.setInterpolation(interp);

            for (KeyframeCell cell : this.keyframes)
            {
                cell.keyframe.setInterpolation(interp);
            }
        }
    }

    /**
     * Keyframe cell
     * 
     * Links a keyframe back to its parent channel
     */
    public static class KeyframeCell
    {
        public Keyframe keyframe;
        public KeyframeChannel channel;

        public KeyframeCell(Keyframe keyframe, KeyframeChannel channel)
        {
            this.keyframe = keyframe;
            this.channel = channel;
        }
    }
}