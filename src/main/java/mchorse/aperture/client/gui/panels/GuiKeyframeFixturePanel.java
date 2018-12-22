package mchorse.aperture.client.gui.panels;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Interpolation;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Area;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.widgets.buttons.GuiCirculate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class GuiKeyframeFixturePanel extends GuiAbstractFixturePanel<KeyframeFixture>
{
    public KeyframeChannel active;
    public int selected = -1;

    public GuiTrackpadElement tick;
    public GuiTrackpadElement value;
    public Area frames = new Area();

    public GuiElements<GuiButtonElement> buttons;
    public GuiElements<GuiButtonElement> frameButtons;

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
    public GuiButtonElement<GuiCirculate> interp;
    public GuiButtonElement<GuiCirculate> easing;

    private boolean sliding = false;
    private int channel = 0;
    private String[] titles = new String[8];

    private boolean dragging = false;
    private boolean moving = false;
    private boolean scrolling = false;
    private int which = 0;
    private int lastX;
    private int lastY;
    private int lastH;
    private int lastV;
    private int lastSX;
    private int lastSY;

    private int shiftX = 0;
    private int shiftY = 0;
    private float zoomY = 1;
    private AllKeyframeChannel allChannel = new AllKeyframeChannel();

    public GuiKeyframeFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.tick = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.tick"), (value) ->
        {
            this.active.get(this.selected).setTick(value.longValue());
            this.sliding = true;
        });

        this.value = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.value"), (value) ->
        {
            this.active.get(this.selected).setValue(value);
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
        this.interp = new GuiButtonElement<GuiCirculate>(mc, new GuiCirculate(0, 0, 0, 80, 20), (b) -> this.changeInterpolation());
        this.interp.button.addLabel("Constant");
        this.interp.button.addLabel("Linear");
        this.interp.button.addLabel("Quadratic");
        this.interp.button.addLabel("Cubic");
        this.interp.button.addLabel("Exponential");
        this.interp.button.addLabel("Bezier");
        this.easing = new GuiButtonElement<GuiCirculate>(mc, new GuiCirculate(0, 0, 0, 80, 20), (b) -> this.changeEasing());
        this.easing.button.addLabel("Ease in");
        this.easing.button.addLabel("Ease out");
        this.easing.button.addLabel("Ease in/out");

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

        this.frameButtons.add(this.interp);
        this.frameButtons.add(this.easing);

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

        this.children.add(this.buttons, this.frameButtons, this.tick, this.value);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.frames.set(0, this.area.y + this.area.h / 2, this.editor.width, 0);
        this.frames.h = this.area.getY(1) - this.frames.y - 25;
    }

    private void addKeyframe()
    {
        Position pos = new Position(Minecraft.getMinecraft().thePlayer);
        float value = 0;

        if (this.active == this.fixture.x) value = pos.point.x;
        if (this.active == this.fixture.y) value = pos.point.y;
        if (this.active == this.fixture.z) value = pos.point.z;
        if (this.active == this.fixture.yaw) value = pos.angle.yaw;
        if (this.active == this.fixture.pitch) value = pos.angle.pitch;
        if (this.active == this.fixture.roll) value = pos.angle.roll;
        if (this.active == this.fixture.fov) value = pos.angle.fov;

        Easing easing = Easing.IN;
        Interpolation interp = Interpolation.LINEAR;
        long tick = this.editor.scrub.value - this.currentOffset();
        long oldTick = tick;

        if (this.selected != -1)
        {
            Keyframe frame = this.active.get(this.selected);

            easing = frame.easing;
            interp = frame.interp;
            oldTick = frame.tick;
        }

        this.selected = this.active.insert(tick, value);

        if (this.active == this.allChannel)
        {
            AllKeyframe allStar = (AllKeyframe) this.allChannel.get(this.selected);

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
            Keyframe frame = this.active.get(this.selected);

            frame.setEasing(easing);
            frame.setInterpolation(interp);
        }

        this.fillData(this.active.get(this.selected));

        if (this.active.getKeyframes().size() == 1)
        {
            this.shiftX = 0;
            this.shiftY = (int) this.active.get(0).value;
        }
    }

    private void removeKeyframe()
    {
        if (this.selected == -1)
        {
            return;
        }

        this.active.remove(this.selected);
        this.selected -= 1;
        this.editor.updateProfile();

        if (this.selected != -1)
        {
            this.fillData(this.active.get(this.selected));
        }
    }

    private void changeInterpolation()
    {
        if (this.selected != -1)
        {
            this.active.get(this.selected).setInterpolation(Interpolation.values()[this.interp.button.getValue()]);
            this.editor.updateProfile();
        }
    }

    private void changeEasing()
    {
        if (this.selected != -1)
        {
            this.active.get(this.selected).setEasing(Easing.values()[this.easing.button.getValue()]);
            this.editor.updateProfile();
        }
    }

    @Override
    public void select(KeyframeFixture fixture, long duration)
    {
        boolean same = this.fixture == fixture;

        super.select(fixture, duration);

        this.allChannel.setFixture(fixture);

        if (!same)
        {
            this.shiftX = 0;
            this.zoomY = 1;
            this.selectChannel(fixture.x);
        }

        if (duration != -1)
        {
            int i = 0;

            for (Keyframe frame : this.active.getKeyframes())
            {
                if (frame.tick >= duration)
                {
                    this.selected = i;

                    break;
                }

                i++;
            }
        }

        if (this.selected != -1)
        {
            this.fillData(this.active.get(this.selected));
        }
    }

    public void fillData(Keyframe frame)
    {
        this.tick.setValue(frame.tick);
        this.value.setValue(frame.value);
        this.interp.button.setValue(frame.interp.ordinal());
        this.easing.button.setValue(frame.easing.ordinal());
    }

    public void selectChannel(KeyframeChannel channel)
    {
        this.active = channel;
        this.selected = -1;
        this.channel = 0;

        if (channel == this.fixture.x) this.channel = 1;
        if (channel == this.fixture.y) this.channel = 2;
        if (channel == this.fixture.z) this.channel = 3;
        if (channel == this.fixture.yaw) this.channel = 4;
        if (channel == this.fixture.pitch) this.channel = 5;
        if (channel == this.fixture.roll) this.channel = 6;
        if (channel == this.fixture.fov) this.channel = 7;

        if (this.active == this.allChannel)
        {
            this.allChannel.setFixture(this.fixture);
        }

        this.shiftY = 0;

        if (!channel.isEmpty())
        {
            this.shiftY = (int) channel.get(0).value;
        }
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
    public void keyTyped(char typedChar, int keyCode)
    {
        super.keyTyped(typedChar, keyCode);

        if (this.selected != -1)
        {
            this.tick.keyTyped(typedChar, keyCode);
            this.value.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (super.mouseClicked(mouseX, mouseY, mouseButton))
        {
            return true;
        }

        /* Select current point with a mouse click */
        if (this.frames.isInside(mouseX, mouseY))
        {
            if (mouseButton == 0)
            {
                Keyframe prev = null;
                int index = 0;

                for (Keyframe frame : this.active.getKeyframes())
                {
                    boolean left = prev != null && prev.interp == Interpolation.BEZIER && this.isInside(frame.tick - frame.lx, frame.value + frame.ly, mouseX, mouseY);
                    boolean right = frame.interp == Interpolation.BEZIER && this.isInside(frame.tick + frame.rx, frame.value + frame.ry, mouseX, mouseY);
                    boolean point = this.isInside(frame.tick, frame.value, mouseX, mouseY);

                    if ((left || right && this.active != this.allChannel) || point)
                    {
                        this.which = left ? 1 : (right ? 2 : 0);
                        this.selected = index;
                        this.fillData(frame);

                        this.lastX = mouseX;
                        this.lastY = mouseY;
                        this.dragging = true;

                        break;
                    }

                    prev = frame;
                    index++;
                }
            }
            else if (mouseButton == 2)
            {
                this.scrolling = true;
                this.lastH = mouseX;
                this.lastV = mouseY;
                this.lastSX = this.shiftX;
                this.lastSY = this.shiftY;
            }

            return true;
        }

        return false;
    }

    private boolean isInside(float tick, float value, int mouseX, int mouseY)
    {
        int x = this.toGraphX(tick);
        int y = this.toGraphY(value);
        double d = Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2);

        return Math.sqrt(d) < 4;
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, int scroll)
    {
        if (super.mouseScrolled(mouseX, mouseY, scroll))
        {
            return true;
        }

        if (this.area.isInside(mouseX, mouseY))
        {
            this.zoomY += Math.copySign(0.2F, scroll);
            this.zoomY = MathHelper.clamp_float(this.zoomY, 0.1F, 100);

            return true;
        }

        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (this.selected != -1)
        {
            if (this.sliding)
            {
                /* Resort after dragging the tick thing */
                Keyframe frame = this.active.get(this.selected);

                this.active.sort();
                this.sliding = false;
                this.selected = this.active.getKeyframes().indexOf(frame);
            }

            if (this.moving)
            {
                this.editor.updateProfile();
            }

            this.dragging = false;
            this.moving = false;
        }

        this.scrolling = false;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        int w = this.editor.width;
        int h = this.editor.height;

        if (this.dragging && !this.moving && (Math.abs(this.lastX - mouseX) > 3 || Math.abs(this.lastY - mouseY) > 3))
        {
            this.moving = true;
            this.sliding = true;
        }

        Gui.drawRect(this.frames.x, this.frames.y, this.frames.x + this.frames.w, this.frames.y + this.frames.h, 0x88000000);
        GuiUtils.scissor(this.frames.x, this.frames.y, this.frames.w, this.frames.h, w, h);

        this.drawGraph(mouseX, mouseY, w, h);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        /* Draw title of the channel */
        this.editor.drawCenteredString(this.font, this.titles[this.channel], w / 2, this.frames.y - this.font.FONT_HEIGHT - 5, 0xffffff);

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }

    /**
     * Render the graph 
     */
    private void drawGraph(int mouseX, int mouseY, int w, int h)
    {
        if (this.active.isEmpty())
        {
            return;
        }

        if (this.scrolling)
        {
            this.shiftX = this.lastSX - (mouseX - this.lastH);
            this.shiftY = this.lastSY + (int) ((mouseY - this.lastV) / this.zoomY);
        }
        /* Move the current keyframe */
        else if (this.moving)
        {
            Keyframe frame = this.active.get(this.selected);
            long x = this.fromGraphX(mouseX);
            float y = this.fromGraphY(mouseY);

            if (this.which == 0)
            {
                frame.setTick(x);
                frame.setValue(y);
            }
            else if (this.which == 1)
            {
                frame.lx = -(x - frame.tick);
                frame.ly = y - frame.value;
            }
            else if (this.which == 2)
            {
                frame.rx = x - frame.tick;
                frame.ry = y - frame.value;
            }

            this.fillData(frame);
        }

        int leftBorder = this.toGraphX(0);
        int rightBorder = this.toGraphX(this.fixture.getDuration());

        if (leftBorder > 0) Gui.drawRect(0, this.frames.y, leftBorder, this.frames.y + this.frames.h, 0x88000000);
        if (rightBorder < w) Gui.drawRect(rightBorder, this.frames.y, w, this.frames.y + this.frames.h, 0x88000000);

        /* Draw graph of the keyframe channel */
        GL11.glLineWidth(2);
        GL11.glPointSize(8);
        GlStateManager.disableTexture2D();

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        GlStateManager.enableBlend();

        /* Colorize the graph for given channel */
        if (this.channel == 0) GlStateManager.color(1F, 0.078F, 0.576F, 0.9F);
        else if (this.channel == 1) GlStateManager.color(0.9F, 0.1F, 0.2F, 0.9F);
        else if (this.channel == 2) GlStateManager.color(0.1F, 0.9F, 0.2F, 0.9F);
        else if (this.channel == 3) GlStateManager.color(0.2F, 0.1F, 0.9F, 0.9F);
        else if (this.channel == 4) GlStateManager.color(0.1F, 0.8F, 0.9F, 0.9F);
        else if (this.channel == 5) GlStateManager.color(0.8F, 0.1F, 0.9F, 0.9F);
        else if (this.channel == 6) GlStateManager.color(0.9F, 0.8F, 0.1F, 0.9F);
        else if (this.channel == 7) GlStateManager.color(0.75F, 0.75F, 0.75F, 0.9F);
        else GlStateManager.color(1, 1, 1, 0.9F);

        /* Draw lines */
        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        Keyframe prev = null;
        boolean all = this.active == this.allChannel;

        for (Keyframe frame : this.active.getKeyframes())
        {
            if (prev != null)
            {
                int px = this.toGraphX(prev.tick);
                int fx = this.toGraphX(frame.tick);

                if (prev.interp == Interpolation.LINEAR)
                {
                    vb.pos(px, this.toGraphY(prev.value), 0).endVertex();
                    vb.pos(fx, this.toGraphY(frame.value), 0).endVertex();
                }
                else
                {
                    for (int i = 0; i < 10; i++)
                    {
                        vb.pos(px + (fx - px) * (i / 10F), this.toGraphY(prev.interpolate(frame, i / 10F)), 0).endVertex();
                        vb.pos(px + (fx - px) * ((i + 1) / 10F), this.toGraphY(prev.interpolate(frame, (i + 1) / 10F)), 0).endVertex();
                    }
                }

                if (prev.interp == Interpolation.BEZIER && !all)
                {
                    vb.pos(this.toGraphX(frame.tick - frame.lx), this.toGraphY(frame.value + frame.ly), 0).endVertex();
                    vb.pos(this.toGraphX(frame.tick), this.toGraphY(frame.value), 0).endVertex();
                }
            }

            if (prev == null)
            {
                vb.pos(0, this.toGraphY(frame.value), 0).endVertex();
                vb.pos(this.toGraphX(frame.tick), this.toGraphY(frame.value), 0).endVertex();
            }

            if (frame.interp == Interpolation.BEZIER && !all)
            {
                vb.pos(this.toGraphX(frame.tick), this.toGraphY(frame.value), 0).endVertex();
                vb.pos(this.toGraphX(frame.tick + frame.rx), this.toGraphY(frame.value + frame.ry), 0).endVertex();
            }

            prev = frame;
        }

        vb.pos(this.toGraphX(prev.tick), this.toGraphY(prev.value), 0).endVertex();
        vb.pos(w, this.toGraphY(prev.value), 0).endVertex();

        Tessellator.getInstance().draw();

        /* Draw points */
        int i = 0;
        prev = null;

        for (Keyframe frame : this.active.getKeyframes())
        {
            GL11.glBegin(GL11.GL_POINTS);
            if (this.selected == i)
            {
                GL11.glColor3f(0, 0.5F, 1);
            }
            else
            {
                GL11.glColor3f(1, 1, 1);
            }

            GL11.glVertex2f(this.toGraphX(frame.tick), this.toGraphY(frame.value));

            if (frame.interp == Interpolation.BEZIER && !all)
            {
                GL11.glVertex2f(this.toGraphX(frame.tick + frame.rx), this.toGraphY(frame.value + frame.ry));
            }

            if (prev != null && prev.interp == Interpolation.BEZIER && !all)
            {
                GL11.glVertex2f(this.toGraphX(frame.tick - frame.lx), this.toGraphY(frame.value + frame.ly));
            }

            GL11.glEnd();

            prev = frame;
            i++;
        }

        /* Draw current point at the scrub */
        int cx = (int) (this.editor.scrub.value - this.currentOffset());
        int cy = this.toGraphY(this.active.interpolate(cx));

        cx = this.toGraphX(cx);

        Gui.drawRect(cx - 1, cy - 1, cx + 1, h, 0xff57f52a);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private int toGraphX(float tick)
    {
        return (int) tick * 2 - this.shiftX;
    }

    private int toGraphY(float value)
    {
        return (int) (-(value - this.shiftY) * this.zoomY) + (this.frames.y + this.frames.h / 2);
    }

    private long fromGraphX(int mouseX)
    {
        return (long) (mouseX + this.shiftX) / 2;
    }

    private float fromGraphY(int mouseY)
    {
        return -(mouseY - (this.frames.y + this.frames.h / 2)) / this.zoomY + this.shiftY;
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
                    allStar.setEasing(kf.easing);
                    allStar.setInterpolation(kf.interp);
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