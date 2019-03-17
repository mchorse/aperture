package mchorse.aperture.client.gui.utils;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Interpolation;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

public class GuiGraphElement extends GuiElement
{
    public GuiAbstractFixturePanel<? extends AbstractFixture> parent;
    public Consumer<Keyframe> callback;

    public KeyframeChannel channel;
    public int duration;
    public int color;
    public int selected = -1;

    public boolean sliding = false;
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
    private float zoomX = 1;
    private float zoomY = 1;
    private int multX = 1;
    private int multY = 1;

    public GuiGraphElement(Minecraft mc, Consumer<Keyframe> callback)
    {
        super(mc);

        this.callback = callback;
    }

    public Keyframe getCurrent()
    {
        return this.channel.get(this.selected);
    }

    /* Graphing code */

    public int toGraphX(float tick)
    {
        return (int) (tick * this.zoomX - this.shiftX) + this.area.x;
    }

    public int toGraphY(float value)
    {
        return (int) (-value * this.zoomY + this.shiftY) + this.area.y;
    }

    public float fromGraphX(int mouseX)
    {
        return (mouseX - this.area.x + this.shiftX) / this.zoomX;
    }

    public float fromGraphY(int mouseY)
    {
        return -(mouseY - this.area.y - this.shiftY) / this.zoomY;
    }

    /**
     * Resets the view  
     */
    public void resetView()
    {
        this.shiftX = 0;
        this.shiftY = 0;
        this.zoomX = 2;
        this.zoomY = 2;

        int c = this.channel.getKeyframes().size();

        if (c > 1)
        {
            Keyframe first = this.channel.get(0);
            Keyframe last = this.channel.get(c - 1);

            float minX = first.tick - 5;
            float minY = Math.min(first.value, last.value);
            float maxX = last.tick + 5;
            float maxY = Math.max(first.value, last.value);

            if (Math.abs(maxY - minY) < 0.01F)
            {
                /* Centerize */
                this.shiftY = (int) ((minY - this.area.h / 2 / this.zoomY) * this.zoomY);
            }
            else
            {
                /* Spread apart vertically */
                this.zoomY = 1 / ((maxY - minY) / this.area.h);
                this.shiftY = (int) (minY * this.zoomY);
            }

            /* Spread apart horizontally */
            this.zoomX = 1 / ((maxX - minX) / this.area.w);
            this.shiftX = (int) (minX * this.zoomX);
        }
        else if (c > 0)
        {
            this.shiftY = (int) ((this.channel.get(0).value - this.area.h / 2 / this.zoomY) * this.zoomY);
        }

        this.recalcMultipliers();
    }

    /**
     * Recalculate grid's multipliers 
     */
    private void recalcMultipliers()
    {
        this.multX = this.recalcMultiplier(this.zoomX);
        this.multY = this.recalcMultiplier(this.zoomY);
    }

    private int recalcMultiplier(float zoom)
    {
        int factor = (int) (60F / zoom);

        /* Hardcoded caps */
        if (factor > 10000) factor = 10000;
        else if (factor > 5000) factor = 5000;
        else if (factor > 2500) factor = 2500;
        else if (factor > 1000) factor = 1000;
        else if (factor > 500) factor = 500;
        else if (factor > 250) factor = 250;
        else if (factor > 100) factor = 100;
        else if (factor > 50) factor = 50;
        else if (factor > 25) factor = 25;
        else if (factor > 10) factor = 10;
        else if (factor > 5) factor = 5;

        return factor <= 0 ? 1 : factor;
    }

    /**
     * Make current keyframe by given duration 
     */
    public void selectByDuration(long duration)
    {
        int i = 0;
        this.selected = -1;

        for (Keyframe frame : this.channel.getKeyframes())
        {
            if (frame.tick >= duration)
            {
                this.selected = i;

                break;
            }

            i++;
        }

        this.setKeyframe(this.getCurrent());
    }

    private void setKeyframe(Keyframe current)
    {
        if (this.callback != null)
        {
            this.callback.accept(current);
        }
    }

    /* Input handling */

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
                Keyframe prev = null;
                int index = 0;

                for (Keyframe frame : this.channel.getKeyframes())
                {
                    boolean left = prev != null && prev.interp == Interpolation.BEZIER && this.isInside(frame.tick - frame.lx, frame.value + frame.ly, mouseX, mouseY);
                    boolean right = frame.interp == Interpolation.BEZIER && this.isInside(frame.tick + frame.rx, frame.value + frame.ry, mouseX, mouseY);
                    boolean point = this.isInside(frame.tick, frame.value, mouseX, mouseY);

                    if (left || right || point)
                    {
                        this.which = left ? 1 : (right ? 2 : 0);
                        this.selected = index;
                        this.setKeyframe(frame);

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
            if (!Minecraft.IS_RUNNING_ON_MAC)
            {
                scroll = -scroll;
            }

            boolean x = GuiScreen.isShiftKeyDown();
            boolean y = GuiScreen.isCtrlKeyDown();
            boolean none = !x && !y;

            /* Scaling X */
            float scaleX = this.zoomX;
            int valueX = (int) (this.fromGraphX(mouseX) * scaleX);

            if (x && !y || none)
            {
                this.zoomX += Math.copySign(this.getZoomFactor(scaleX), scroll);
                this.zoomX = MathHelper.clamp(this.zoomX, 0.01F, 10F);
            }

            if (this.zoomX != scaleX)
            {
                this.shiftX = (int) ((valueX - (valueX - this.shiftX) * (scaleX / this.zoomX)) * (this.zoomX / scaleX));
            }

            /* Scaling Y */
            float scaleY = this.zoomY;
            int valueY = (int) (this.fromGraphY(mouseY) * scaleY);

            if (y && !x || none)
            {
                this.zoomY += Math.copySign(this.getZoomFactor(scaleY), scroll);
                this.zoomY = MathHelper.clamp(this.zoomY, 0.01F, 10F);
            }

            if (this.zoomY != scaleY)
            {
                this.shiftY = (int) ((valueY - (valueY - this.shiftY) * (scaleY / this.zoomY)) * (this.zoomY / scaleY));
            }

            this.recalcMultipliers();

            return true;
        }

        return false;
    }

    /**
     * Get zoom factor based by current zoom value 
     */
    private float getZoomFactor(float zoom)
    {
        float factor = 0.1F;

        if (zoom < 0.1F) factor = 0.005F;
        else if (zoom < 1) factor = 0.05F;

        return factor;
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
                Keyframe frame = this.channel.get(this.selected);

                this.channel.sort();
                this.sliding = false;
                this.selected = this.channel.getKeyframes().indexOf(frame);
            }

            if (this.moving && this.parent != null)
            {
                this.parent.editor.updateProfile();
            }

            this.dragging = false;
            this.moving = false;
        }

        this.scrolling = false;
    }

    /* Rendering */

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        GuiScreen screen = this.mc.currentScreen;
        int w = screen.width;
        int h = screen.height;

        if (this.dragging && !this.moving && (Math.abs(this.lastX - mouseX) > 3 || Math.abs(this.lastY - mouseY) > 3))
        {
            this.moving = true;
            this.sliding = true;
        }

        Gui.drawRect(this.area.x, this.area.y, this.area.getX(1), this.area.getY(1), 0x88000000);
        GuiUtils.scissor(this.area.x, this.area.y, this.area.w, this.area.h, w, h);

        this.drawGraph(mouseX, mouseY, w, h);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }

    /**
     * Render the graph 
     */
    private void drawGraph(int mouseX, int mouseY, int w, int h)
    {
        if (this.channel.isEmpty())
        {
            return;
        }

        if (this.scrolling)
        {
            this.shiftX = this.lastSX - (mouseX - this.lastH);
            this.shiftY = this.lastSY + (mouseY - this.lastV);
        }
        /* Move the current keyframe */
        else if (this.moving)
        {
            Keyframe frame = this.channel.get(this.selected);
            long x = (long) this.fromGraphX(!GuiScreen.isShiftKeyDown() ? mouseX : this.lastX);
            float y = this.fromGraphY(!GuiScreen.isCtrlKeyDown() ? mouseY : this.lastY);

            if (this.which == 0)
            {
                frame.setTick(x);
                frame.setValue(y);
            }
            else if (this.which == 1)
            {
                frame.lx = -(x - frame.tick);
                frame.ly = y - frame.value;

                if (!GuiScreen.isAltKeyDown())
                {
                    frame.rx = frame.lx;
                    frame.ry = -frame.ly;
                }
            }
            else if (this.which == 2)
            {
                frame.rx = x - frame.tick;
                frame.ry = y - frame.value;

                if (!GuiScreen.isAltKeyDown())
                {
                    frame.lx = frame.rx;
                    frame.ly = -frame.ry;
                }
            }

            this.setKeyframe(this.getCurrent());
        }

        int leftBorder = this.toGraphX(0);
        int rightBorder = this.toGraphX(this.duration);

        if (leftBorder > 0) Gui.drawRect(0, this.area.y, leftBorder, this.area.y + this.area.h, 0x88000000);
        if (rightBorder < w) Gui.drawRect(rightBorder, this.area.y, w, this.area.y + this.area.h, 0x88000000);

        /* Draw scaling grid */
        int hx = this.duration / this.multX;

        for (int j = 0; j <= hx; j++)
        {
            int x = this.toGraphX(j * this.multX);

            Gui.drawRect(this.area.x + x, this.area.y, this.area.x + x + 1, this.area.getY(1), 0x44ffffff);
            this.font.drawString(String.valueOf(j * this.multX), this.area.x + x + 4, this.area.y + 4, 0xffffff);
        }

        int ty = (int) this.fromGraphY(this.area.getY(1));
        int by = (int) this.fromGraphY(this.area.y);

        int min = Math.min(ty, by) - 1;
        int max = Math.max(ty, by) + 1;
        int mult = this.multY;

        min -= min % mult + mult;
        max -= max % mult - mult;

        for (int j = 0, c = (max - min) / mult; j < c; j++)
        {
            int y = this.toGraphY(min + j * mult);

            Gui.drawRect(this.area.x, y, this.area.getX(1), y + 1, 0x44ffffff);
            this.font.drawString(String.valueOf(min + j * mult), this.area.x + 4, y + 4, 0xffffff);
        }

        /* Draw graph of the keyframe channel */
        GL11.glLineWidth(2);
        GL11.glPointSize(8);
        GlStateManager.disableTexture2D();

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        GlStateManager.enableBlend();

        /* Colorize the graph for given channel */
        float r = (this.color >> 16 & 255) / 255.0F;
        float g = (this.color >> 8 & 255) / 255.0F;
        float b = (this.color & 255) / 255.0F;

        GlStateManager.color(r, g, b, 0.75F);

        /* Draw lines */
        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        Keyframe prev = null;

        for (Keyframe frame : this.channel.getKeyframes())
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

                if (prev.interp == Interpolation.BEZIER)
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

            if (frame.interp == Interpolation.BEZIER)
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

        for (Keyframe frame : this.channel.getKeyframes())
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

            if (frame.interp == Interpolation.BEZIER)
            {
                GL11.glVertex2f(this.toGraphX(frame.tick + frame.rx), this.toGraphY(frame.value + frame.ry));
            }

            if (prev != null && prev.interp == Interpolation.BEZIER)
            {
                GL11.glVertex2f(this.toGraphX(frame.tick - frame.lx), this.toGraphY(frame.value + frame.ly));
            }

            GL11.glEnd();

            prev = frame;
            i++;
        }

        /* Draw current point at the scrub */
        if (this.parent != null)
        {
            int cx = (int) (this.parent.editor.scrub.value - this.parent.currentOffset());
            int cy = this.toGraphY(this.channel.interpolate(cx));

            cx = this.toGraphX(cx);

            Gui.drawRect(cx - 1, cy - 1, cx + 1, h, 0xff57f52a);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
}