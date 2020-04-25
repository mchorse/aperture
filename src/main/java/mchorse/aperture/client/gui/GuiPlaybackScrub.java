package mchorse.aperture.client.gui;

import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * GUI playback timeline
 *
 * This class is responsible for rendering and controlling the playback
 */
public class GuiPlaybackScrub extends GuiElement
{
    /**
     * Vanilla buttons resource location
     */
    public static final ResourceLocation VANILLA_BUTTONS = new ResourceLocation("textures/gui/widgets.png");

    public boolean scrubbing;
    public int value;
    public int min;
    public int max;
    public int index;

    public GuiCameraEditor editor;
    public CameraProfile profile;

    public int scroll;
    public boolean scrolling;
    public float scale = 1;

    private int lastX;
    private boolean dragging;
    private boolean resize;
    private AbstractFixture start;
    private AbstractFixture end;

    public GuiPlaybackScrub(Minecraft mc, GuiCameraEditor editor, CameraProfile profile)
    {
        super(mc);

        this.editor = editor;
        this.profile = profile;
    }

    @Override
    public void resize()
    {
        super.resize();

        this.clampScroll();
    }

    /* Public API methods  */

    /**
     * Set profile and update values which depends on camera profile
     */
    public void setProfile(CameraProfile profile)
    {
        boolean same = profile == this.profile;

        this.profile = profile;
        this.index = -1;

        this.max = Math.max(profile == null ? 0 : (int) profile.getDuration(), this.editor.maxScrub);
        this.value = MathHelper.clamp(this.value, this.min, this.max);

        if (!same)
        {
            this.scroll = 0;
            this.scale = 1;
        }
    }

    /**
     * Set the value of the scrubber. Also, if the value has changed notify
     * the listener.
     */
    public void setValue(int value, boolean fromScrub)
    {
        int old = this.value;

        this.value = value;
        this.value = MathHelper.clamp(this.value, this.min, this.max);

        if (this.value != old)
        {
            this.editor.setFlight(false);
            this.editor.scrubbed(this, this.value, fromScrub);

            int v = (int) (this.value * this.scale);
            int w = this.area.w - 4;

            if (this.scroll  > v)
            {
                this.scroll = v;
            }
            else if (v > this.scroll + w)
            {
                this.scroll = v - w;
            }

            this.clampScroll();
        }
    }

    /**
     * Set the value of the scrubb using API
     */
    public void setValue(int value)
    {
        this.setValue(value, false);
    }

    /**
     * Set the value of the scrubber from timeline
     */
    public void setValueFromScrub(int value)
    {
        this.setValue(value, true);
    }

    /**
     * Calculate value from given mouse X
     */
    public int calcValueFromMouse(int mouseX)
    {
        int max = this.max == 0 ? 1 : this.max;
        float factor = (mouseX - 2 - this.area.x + this.scroll) / (max * this.scale);

        return (int) (factor * (max - this.min)) + this.min;
    }

    /**
     * Calculate mouse X from given value
     */
    public int calcMouseFromValue(int value)
    {
        int max = this.max == 0 ? 1 : this.max;
        float factor = (value - this.min) / (float) (max - this.min);

        return (int) (factor * max * this.scale) + this.area.x + 2 - this.scroll;
    }

    /**
     * Clamp the scroll value 
     */
    public void clampScroll()
    {
        int max = (int) (this.max * this.scale) - this.area.w + 4;

        this.scroll = MathHelper.clamp(this.scroll, 0, max > 0 ? max : 0);
    }

    /* GUI interactions */

    /**
     * Mouse was clicked
     */
    @Override
    public boolean mouseClicked(GuiContext context)
    {
        int mouseX = context.mouseX;
        int mouseY = context.mouseY;

        if (this.area.isInside(mouseX, mouseY))
        {
            if (context.mouseButton == 0)
            {
                this.scrubbing = true;
                this.setValueFromScrub(this.calcValueFromMouse(mouseX));
            }
            else if (context.mouseButton == 1 && this.profile != null)
            {
                int tick = this.calcValueFromMouse(mouseX);

                if (this.editor.creating)
                {
                    this.editor.addMarker(tick);

                    return false;
                }

                AbstractFixture fixture = this.profile.atTick(tick);
                long offset = this.profile.calculateOffset(fixture);

                if (fixture != null)
                {
                    boolean left = Math.abs(this.calcMouseFromValue((int) offset) - mouseX) < 5;
                    boolean right = Math.abs(this.calcMouseFromValue((int) (offset + fixture.getDuration())) - mouseX) < 5;

                    if (left || right)
                    {
                        int index = this.profile.getAll().indexOf(fixture);

                        if (left && index > 0)
                        {
                            this.dragging = true;
                            this.lastX = mouseX;
                            this.start = this.profile.get(index - 1);
                            this.end = fixture;
                        }
                        else if (right)
                        {
                            this.dragging = true;
                            this.lastX = mouseX;
                            this.start = fixture;
                            this.end = this.profile.has(index + 1) ? this.profile.get(index + 1) : null;
                        }
                    }
                }

                /* Select camera fixture */
                int index = this.profile.getAll().indexOf(fixture);

                this.editor.pickCameraFixture(fixture, tick - offset);
                this.index = index;
            }
            else if (context.mouseButton == 2)
            {
                this.scrolling = true;
                this.lastX = mouseX;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(GuiContext context)
    {
        int scroll = context.mouseWheel;

        if (!Minecraft.IS_RUNNING_ON_MAC)
        {
            scroll = -scroll;
        }

        if (this.area.isInside(context) && !this.scrolling)
        {
            float scale = this.scale;
            float factor = 0.1F;
            int value = (int) (this.calcValueFromMouse(context.mouseX) * scale);

            if (this.scale < 0.1F) factor = 0.005F;
            else if (this.scale < 1) factor = 0.05F;

            this.scale += Math.copySign(factor, scroll);
            this.scale = MathHelper.clamp(this.scale, 0.01F, 10F);

            /* Correct the left pivoted scroll */
            if (this.scale != scale)
            {
                /* I don't know what the fuck is that formula, but I 
                 * spent literally two hours coming up with it so it 
                 * would give correct results for scaling where exactly 
                 * the cursor is...
                 * 
                 * I don't know how it works, don't ask me about it xD
                 */
                this.scroll = (int) ((value - (value - this.scroll) * (scale / this.scale)) * (this.scale / scale));
            }

            this.clampScroll();

            return true;
        }

        return super.mouseScrolled(context);
    }

    /**
     * Mouse was released
     */
    @Override
    public void mouseReleased(GuiContext context)
    {
        if (this.resize)
        {
            this.profile.dirty();
            this.editor.updateValues();
        }

        this.scrubbing = false;
        this.resize = false;
        this.dragging = false;
        this.scrolling = false;
    }

    /**
     * Draw timeline on the screen
     *
     * This timeline looks quite simple. The line part is inspired by Blender's
     * timeline thingy. Scrub also renders all of available camera fixtures.
     */
    @Override
    public void draw(GuiContext context)
    {
        int mouseX = context.mouseX;
        int mouseY = context.mouseY;

        if (this.scrubbing)
        {
            this.setValueFromScrub(this.calcValueFromMouse(mouseX));

            if (this.max * this.scale - this.area.w + 4 > 0)
            {
                int delta = mouseX - this.area.mx();
                int edge = (int) Math.copySign(this.area.w / 2 - 50, -delta) + delta;

                if (Math.copySign(1, edge) == Math.copySign(1, delta) && delta != 0)
                {
                    float factor = edge / 50F;
                    float scaleFactor = this.scale <= 1 ? 1F / this.scale : this.scale;

                    this.scroll += factor * scaleFactor;
                    this.clampScroll();
                }
            }
        }

        if (this.scrolling)
        {
            this.scroll -= (mouseX - this.lastX);
            this.lastX = mouseX;

            this.clampScroll();
        }

        /* Visual duration resize */
        if (this.dragging && Math.abs(mouseX - this.lastX) > 6 && !this.resize)
        {
            this.resize = true;
        }

        if (this.resize && this.profile != null)
        {
            long start = this.profile.calculateOffset(this.start);
            long end = start + this.start.getDuration();

            if (this.end != null)
            {
                end += this.end.getDuration();
            }

            long value = this.calcValueFromMouse(mouseX);

            if (value >= start + 5 && (this.end == null ? true : value <= end - 5))
            {
                this.start.setDuration(value - start);

                if (this.end != null)
                {
                    this.end.setDuration(end - value);
                }

                /* Update the values */
                GuiAbstractFixturePanel<AbstractFixture> delegate = this.editor.panel.delegate;

                if (delegate != null)
                {
                    if (delegate.fixture == this.start)
                    {
                        delegate.duration.setValue(this.start.getDuration());
                    }
                    else if (delegate.fixture == this.end)
                    {
                        delegate.duration.setValue(this.end.getDuration());
                    }
                }
            }
        }

        int x = this.area.x;
        int y = this.area.y;
        int w = this.area.w;
        int h = this.area.h;

        if (this.profile != null)
        {
            /* Calculate tick marker position and tick label width */
            String label = this.value + "/" + this.max;
            int tx = this.calcMouseFromValue(this.value);
            int width = this.font.getStringWidth(label) + 4;

            /* Draw fixtures */
            int pos = 0;
            int i = 0;
            boolean drawnMarker = false;
            int leftMarginMarker = 0;
            int rightMarginMarker = 0;

            GuiDraw.scissor(x + 2, y - 16, w - 4, h + 16, context);

            for (AbstractFixture fixture : this.profile.getAll())
            {
                int color = FixtureRegistry.CLIENT.get(fixture.getClass()).color.getRGBColor();

                boolean selected = i == this.index;
                int leftMargin = this.calcMouseFromValue(pos) - 1;
                int rightMargin = this.calcMouseFromValue(pos + (int) fixture.getDuration()) - 1;

                if (rightMargin < this.area.x)
                {
                    pos += fixture.getDuration();
                    i++;

                    continue;
                }
                else if (leftMargin > this.area.ex())
                {
                    break;
                }

                /* Draw fixture's background and the hinge */
                Gui.drawRect(leftMargin + 1, y + (selected ? 12 : 15), rightMargin, y + h - 1, (selected ? 0xdd000000 : 0x66000000) + color);
                Gui.drawRect(rightMargin, y + 1, rightMargin + 1, y + h - 1, 0xff000000 + color);

                if (selected)
                {
                    this.drawGradientRect(leftMargin + 1, y + 5, rightMargin, y + 12, 0x000088ff, 0x880088ff);
                    this.drawGradientRect(leftMargin + 1, y + 15, rightMargin, y + h - 1, 0x00000000, 0x44000000);
                }

                String name = fixture.getName();

                /* Draw path's fixture separators */
                if (fixture instanceof PathFixture)
                {
                    PathFixture path = (PathFixture) fixture;
                    int c = path.getCount() - 1;

                    if (c > 1)
                    {
                        if (path.perPointDuration)
                        {
                            long duration = path.getDuration();
                            long frame = path.getPoint(0).getDuration();

                            for (int j = 1; j < c; j++)
                            {
                                int fract = (int) ((rightMargin - leftMargin) * ((float) frame / duration));
                                int px = leftMargin + fract;

                                Gui.drawRect(px, y + 5, px + 1, y + h - 1, 0xff000000 + color - 0x00181818);

                                frame += path.getPoint(j).getDuration();
                            }
                        }
                        else
                        {
                            float fract = (rightMargin - leftMargin) / (float) c;

                            for (int j = 1; j < c; j++)
                            {
                                int px = leftMargin + (int) (fract * j);

                                Gui.drawRect(px, y + 5, px + 1, y + h - 1, 0xff000000 + color - 0x00181818);
                            }
                        }
                    }
                }

                if (this.area.isInside(mouseX, mouseY) && !this.resize && !drawnMarker && i != 0)
                {
                    boolean left = Math.abs(leftMargin - mouseX) < 5;
                    boolean right = Math.abs(rightMargin - mouseX) < 5;

                    if (left || right)
                    {
                        leftMarginMarker = leftMargin;
                        rightMarginMarker = rightMargin;
                        drawnMarker = true;
                    }
                }

                /* Draw fixture's title */
                if (!name.isEmpty())
                {
                    int lw = this.font.getStringWidth(name);
                    int textColor = selected ? 16777120 : 0xffffff;
                    boolean shortened = false;

                    while (lw + 10 >= rightMargin - leftMargin && !name.isEmpty())
                    {
                        name = name.substring(0, name.length() - 1);
                        lw = this.font.getStringWidth(name);
                        shortened = true;
                    }

                    if (!name.isEmpty())
                    {
                        this.font.drawStringWithShadow(shortened ? name + "..." : name, leftMargin + 4, y + 6, textColor);
                    }
                }

                pos += fixture.getDuration();
                i++;
            }

            if (this.editor.creating)
            {
                for (Integer marker : this.editor.markers)
                {
                    int mx = this.calcMouseFromValue(marker);

                    Gui.drawRect(mx, y + 5, mx + 1, y + h - 1,0xaaff0000);
                }
            }

            /* Draw shadows to indicate that there are still stuff to scroll */
            if (this.scroll > this.min * this.scale) GuiDraw.drawHorizontalGradientRect(x + 2, y + h - 5, x + 22, y + h, 0x88000000, 0x00000000, 0);
            if (this.scroll < this.max * this.scale - this.area.w + 4) GuiDraw.drawHorizontalGradientRect(x + w - 22, y + h - 5, x + w - 2, y + h, 0x00000000, 0x88000000, 0);

            /* Draw end marker and also shadow of area where there is no  */
            int stopX = this.calcMouseFromValue((int) this.profile.getDuration());

            if (stopX < this.area.ex() - 2)
            {
                this.drawGradientRect(stopX + 1, y + h / 2, x + w - 1, y + h, 0x00, 0x88000000);
                Gui.drawRect(stopX, y + h / 2, stopX + 1, y + h, 0xaaffffff);
            }

            /* Draw the marker */
            Gui.drawRect(tx, y + 1, tx + 2, y + h - 1, 0xff57f52a);

            /* Draw the "how far into fixture" tick */
            String offsetLabel = String.valueOf(this.value - this.profile.calculateOffset(this.value, false));
            int ow = this.font.getStringWidth(offsetLabel);

            this.font.drawStringWithShadow(offsetLabel, tx - ow / 2 + 1, y + h - this.font.FONT_HEIGHT * 3 - 1, 0xffffff);

            /* Move the tick line left, so it won't overflow the timeline */
            if (tx + 3 - x + width > w)
            {
                tx -= width + 2;
            }

            /* Draw the tick label */
            Gui.drawRect(tx + 2, y + h - 3 - this.font.FONT_HEIGHT, tx + 2 + width, y + h - 1, 0xaa57f52a);
            this.font.drawStringWithShadow(label, tx + 4, y + h - this.font.FONT_HEIGHT - 1, 0xffffff);

            GuiDraw.unscissor(context);

            /* Draw resizing markers */
            if (drawnMarker)
            {
                int markerOffset = (Math.abs(leftMarginMarker - mouseX) < 5 ? leftMarginMarker : rightMarginMarker);

                Gui.drawRect(markerOffset - 4, this.area.y - 1, markerOffset + 5, this.area.y, 0xaaffffff);
                Gui.drawRect(markerOffset - 5, this.area.y - 1 - 2, markerOffset - 4, this.area.y + 2, 0xaaffffff);
                Gui.drawRect(markerOffset + 5, this.area.y - 1 - 2, markerOffset + 6, this.area.y + 2, 0xaaffffff);
            }
        }

        /* Draw background */
        Gui.drawRect(x + 1, y + h / 2, x + 2, y + h, 0xaaffffff);
        Gui.drawRect(x + w - 2, y + h / 2, x + w - 1, y + h, 0xaaffffff);
        Gui.drawRect(x, y + h - 1, x + w, y + h, 0xffffffff);
    }

    /**
     * Scrub event listener
     */
    public static interface IScrubListener
    {
        public void scrubbed(GuiPlaybackScrub scrub, int value, boolean fromScrub);
    }
}