package mchorse.aperture.client.gui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mchorse.aperture.client.gui.GuiCameraEditor;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeInterpolation;
import mchorse.aperture.client.gui.panels.keyframe.AllKeyframe;
import mchorse.aperture.client.gui.panels.keyframe.AllKeyframeChannel;
import mchorse.aperture.client.gui.panels.keyframe.KeyframeCell;
import mchorse.aperture.utils.Scale;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiDopeSheet extends GuiKeyframeElement
{
    public List<GuiSheet> sheets = new ArrayList<GuiSheet>();
    public Scale scale = new Scale(false);
    public GuiSheet current;
    public int duration;

    public boolean sliding = false;
    public boolean dragging = false;
    private boolean moving = false;
    private boolean scrolling = false;
    public int which = 0;
    private int lastX;
    private float lastT;

    public GuiDopeSheet(Minecraft mc, Consumer<Keyframe> callback)
    {
        super(mc, callback);
    }

    /* Graphing code */

    public int toGraph(float tick)
    {
        return (int) (this.scale.to(tick)) + this.area.getX(0.5F);
    }

    public float fromGraph(int mouseX)
    {
        return this.scale.from(mouseX - this.area.getX(0.5F));
    }

    public void resetView()
    {
        this.scale.set(0, 2);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        /* Find minimum and maximum */
        for (GuiSheet sheet : this.sheets)
        {
            for (Keyframe frame : sheet.channel.getKeyframes())
            {
                min = Integer.min((int) frame.tick, min);
                max = Integer.max((int) frame.tick, max);
            }
        }

        if (Math.abs(max - min) > 0.01F)
        {
            this.scale.view(min, max, this.area.w, 30);
        }

        this.recalcMultipliers();
    }

    /**
     * Recalculate grid's multipliers 
     */
    private void recalcMultipliers()
    {
        this.scale.mult = this.recalcMultiplier(this.scale.zoom);
    }

    @Override
    public Keyframe getCurrent()
    {
        if (this.current != null)
        {
            return this.current.getKeyframe();
        }

        return null;
    }

    @Override
    public void setDuration(long duration)
    {
        this.duration = (int) duration;
    }

    @Override
    public void setSliding()
    {}

    @Override
    public void selectByDuration(long duration)
    {}

    @Override
    public void doubleClick(int mouseX, int mouseY)
    {
        if (this.which == -1)
        {
            int count = this.sheets.size();
            int h = (this.area.h - 15) / count;
            int i = (mouseY - (this.area.getY(1) - h * count)) / h;

            if (i < 0 || i >= count)
            {
                return;
            }

            this.current = this.sheets.get(i);

            Easing easing = Easing.IN;
            KeyframeInterpolation interp = KeyframeInterpolation.LINEAR;
            Keyframe frame = this.getCurrent();
            long tick = (long) this.fromGraph(mouseX);
            long oldTick = tick;

            if (frame != null)
            {
                easing = frame.easing;
                interp = frame.interp;
                oldTick = frame.tick;
            }

            this.current.selected = this.current.channel.insert(tick, this.current.channel.interpolate(tick));
            frame = this.getCurrent();

            if (oldTick != tick)
            {
                frame.setEasing(easing);
                frame.setInterpolation(interp);
            }

            if (frame instanceof AllKeyframe)
            {
                AllKeyframeChannel all = (AllKeyframeChannel) this.current.channel;
                AllKeyframe key = (AllKeyframe) frame;
                Position pos = new Position(Minecraft.getMinecraft().player);

                float value = 0;

                if (Minecraft.getMinecraft().currentScreen instanceof GuiCameraEditor)
                {
                    pos = new Position(((GuiCameraEditor) Minecraft.getMinecraft().currentScreen).getCamera());
                }

                for (KeyframeChannel channel : all.fixture.channels)
                {
                    if (channel == all.fixture.x) value = (float) pos.point.x;
                    if (channel == all.fixture.y) value = (float) pos.point.y;
                    if (channel == all.fixture.z) value = (float) pos.point.z;
                    if (channel == all.fixture.yaw) value = pos.angle.yaw;
                    if (channel == all.fixture.pitch) value = pos.angle.pitch;
                    if (channel == all.fixture.roll) value = pos.angle.roll;
                    if (channel == all.fixture.fov) value = pos.angle.fov;

                    int index = channel.insert(tick, value);

                    key.keyframes.add(new KeyframeCell(channel.getKeyframes().get(index), channel));
                }
            }
        }
        else if (this.which == 0)
        {
            Keyframe frame = this.getCurrent();

            if (frame == null)
            {
                return;
            }

            this.current.channel.remove(this.current.selected);
            this.current.selected -= 1;
            this.which = -1;
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
        if (this.area.isInside(mouseX, mouseY))
        {
            if (mouseButton == 0)
            {
                /* Duplicate the keyframe */
                if (GuiScreen.isAltKeyDown() && this.current != null && this.which == 0)
                {
                    Keyframe frame = this.getCurrent();

                    if (frame != null)
                    {
                        long offset = (long) this.fromGraph(mouseX);
                        Keyframe created = this.current.channel.get(this.current.channel.insert(offset, frame.value));

                        this.current.selected = this.current.channel.getKeyframes().indexOf(created);
                        created.copy(frame);
                        created.tick = offset;
                    }

                    return false;
                }

                this.which = -1;
                this.current = null;

                int count = this.sheets.size();
                int h = (this.area.h - 15) / count;
                int y = this.area.getY(1) - h * count;
                boolean reset = true;

                for (GuiSheet sheet : this.sheets)
                {
                    int i = 0;
                    sheet.selected = -1;

                    for (Keyframe frame : sheet.channel.getKeyframes())
                    {
                        boolean point = this.isInside(this.toGraph(frame.tick), y + h / 2, mouseX, mouseY);

                        if (point)
                        {
                            this.which = 0;
                            this.current = sheet;
                            this.setKeyframe(frame);

                            this.lastT = frame.tick;

                            this.lastX = mouseX;
                            this.dragging = true;
                            sheet.selected = i;
                            reset = false;

                            break;
                        }

                        i++;
                    }

                    y += h;
                }

                if (this.parent != null && reset)
                {
                    this.dragging = true;
                }
            }
            else if (mouseButton == 2)
            {
                this.scrolling = true;
                this.lastX = mouseX;
                this.lastT = this.scale.shift;
            }
        }

        return false;
    }

    private boolean isInside(float x, float y, int mouseX, int mouseY)
    {
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

        if (this.area.isInside(mouseX, mouseY) && !this.scrolling)
        {
            if (!Minecraft.IS_RUNNING_ON_MAC)
            {
                scroll = -scroll;
            }

            this.scale.zoom(Math.copySign(this.getZoomFactor(this.scale.zoom), scroll), 0.01F, 50F);
            this.recalcMultipliers();

            return true;
        }

        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (this.current != null)
        {
            if (this.sliding)
            {
                /* Resort after dragging the tick thing */
                this.current.sort();
                this.sliding = false;

                for (GuiSheet sheet : this.sheets)
                {
                    if (sheet.channel instanceof AllKeyframeChannel)
                    {
                        AllKeyframeChannel channel = (AllKeyframeChannel) sheet.channel;

                        channel.setFixture(channel.fixture);
                    }
                }
            }

            if (this.moving && this.parent != null)
            {
                this.parent.editor.updateProfile();
            }
        }

        this.dragging = false;
        this.moving = false;
        this.scrolling = false;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        if (this.dragging && !this.moving && (Math.abs(this.lastX - mouseX) > 3))
        {
            this.moving = true;
            this.sliding = true;
        }

        if (this.scrolling)
        {
            this.scale.shift = -(mouseX - this.lastX) / this.scale.zoom + this.lastT;
        }
        /* Move the current keyframe */
        else if (this.moving)
        {
            Keyframe frame = this.getCurrent();
            float x = this.fromGraph(mouseX);

            if (this.which == 0)
            {
                frame.setTick((long) x);
            }
            else if (this.which == -1 && this.parent != null)
            {
                long offset = this.parent.editor.getProfile().calculateOffset(this.parent.fixture);

                this.parent.editor.scrub.setValueFromScrub((int) (x + offset));
            }

            this.setKeyframe(this.getCurrent());
        }

        /* Draw shit */
        this.area.draw(0x88000000);

        int w = this.area.w;
        int leftBorder = (int) this.toGraph(0);
        int rightBorder = (int) this.toGraph(this.duration);

        if (leftBorder > 0) Gui.drawRect(0, this.area.y, leftBorder, this.area.y + this.area.h, 0x88000000);
        if (rightBorder < w) Gui.drawRect(rightBorder, this.area.y, w, this.area.y + this.area.h, 0x88000000);

        /* Draw scaling grid */
        int hx = this.duration / this.scale.mult;

        for (int j = 0; j <= hx; j++)
        {
            int x = (int) this.toGraph(j * this.scale.mult);

            Gui.drawRect(this.area.x + x, this.area.y, this.area.x + x + 1, this.area.getY(1), 0x44ffffff);
            this.font.drawString(String.valueOf(j * this.scale.mult), this.area.x + x + 4, this.area.y + 4, 0xffffff);
        }

        /* Draw current point at the scrub */
        if (this.parent != null)
        {
            int cx = this.getOffset();

            cx = this.toGraph(cx);

            Gui.drawRect(cx - 1, this.area.y, cx + 1, this.area.getY(1), 0xff57f52a);
        }

        /* Draw dope sheet */
        int count = this.sheets.size();
        int h = (this.area.h - 15) / count;
        int y = this.area.getY(1) - h * count;

        for (GuiSheet sheet : this.sheets)
        {
            float r = (sheet.color >> 16 & 255) / 255.0F;
            float g = (sheet.color >> 8 & 255) / 255.0F;
            float b = (sheet.color & 255) / 255.0F;

            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(Minecraft.getMinecraft().gameSettings.guiScale * 1.5F);

            VertexBuffer vb = Tessellator.getInstance().getBuffer();

            vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            vb.pos(this.area.x, y + h / 2, 0).color(r, g, b, 0.65F).endVertex();
            vb.pos(this.area.getX(1), y + h / 2, 0).color(r, g, b, 0.65F).endVertex();

            Tessellator.getInstance().draw();

            int i = 0;
            GL11.glPointSize(Minecraft.getMinecraft().gameSettings.guiScale * 6);
            GL11.glBegin(GL11.GL_POINTS);

            for (Keyframe frame : sheet.channel.getKeyframes())
            {
                if (this.current == sheet && i == sheet.selected)
                {
                    GL11.glColor4f(1, 1, 1, 1);
                }
                else
                {
                    GL11.glColor4f(r * 2, g * 2, b * 2, 1);
                }

                GL11.glVertex2f(this.toGraph(frame.tick), y + h / 2);

                i++;
            }

            GL11.glEnd();

            i = 0;
            GL11.glPointSize(Minecraft.getMinecraft().gameSettings.guiScale * 4);
            GL11.glBegin(GL11.GL_POINTS);

            for (Keyframe frame : sheet.channel.getKeyframes())
            {
                if (this.current == sheet && i == sheet.selected)
                {
                    GL11.glColor4f(0, 0.5F, 1, 1);
                }
                else
                {
                    GL11.glColor4f(0, 0, 0, 1);
                }

                GL11.glVertex2f(this.toGraph(frame.tick), y + h / 2);

                i++;
            }

            GL11.glEnd();

            int lw = this.font.getStringWidth(sheet.title) + 10;
            GuiUtils.drawHorizontalGradientRect(this.area.getX(1) - lw - 10, y, this.area.getX(1), y + h, sheet.color, 0xaa000000 + sheet.color, 0);
            this.font.drawStringWithShadow(sheet.title, this.area.getX(1) - lw + 5, y + (h - this.font.FONT_HEIGHT) / 2 + 1, 0xffffff);

            y += h;
        }

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }

    public static class GuiSheet
    {
        public String title = "";
        public int color;
        public KeyframeChannel channel;
        public int selected = -1;

        public GuiSheet(String title, int color, KeyframeChannel channel)
        {
            this.title = title;
            this.color = color;
            this.channel = channel;
        }

        public void sort()
        {
            Keyframe frame = this.getKeyframe();

            this.channel.sort();
            this.selected = this.channel.getKeyframes().indexOf(frame);
        }

        public Keyframe getKeyframe()
        {
            return this.channel.get(this.selected);
        }
    }
}