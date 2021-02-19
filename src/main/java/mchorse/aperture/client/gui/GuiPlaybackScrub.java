package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.ManualFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Area;
import mchorse.mclib.client.gui.utils.Scale;
import mchorse.mclib.utils.Color;
import mchorse.mclib.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.math.MathHelper;

/**
 * GUI playback timeline
 *
 * This class is responsible for rendering and controlling the playback
 */
public class GuiPlaybackScrub extends GuiElement
{
    public static final Color COLOR = new Color();

    public boolean scrubbing;
    public int value;
    protected int min;
    protected int max;
    public int index;

    public GuiCameraEditor editor;
    public CameraProfile profile;

    private Area timeline = new Area();
    public boolean scrolling;
    public Scale scale;

    private int lastX;
    private boolean dragging;
    private boolean resize;
    private AbstractFixture start;
    private AbstractFixture end;

    private boolean firstTime;

    public GuiPlaybackScrub(Minecraft mc, GuiCameraEditor editor, CameraProfile profile)
    {
        super(mc);

        this.editor = editor;
        this.profile = profile;
        this.scale = new Scale(this.timeline, false);
        this.scale.anchor(0.5F);
    }

    @Override
    public void resize()
    {
        super.resize();

        this.timeline.copy(this.area);
        this.timeline.offsetX(-2);

        if (!this.firstTime)
        {
            this.scale.view(this.min, this.max);
            this.firstTime = true;
        }

        this.clampScroll();
    }

    public void set(int min, int max)
    {
        this.min = min;
        this.max = max;

        if (this.profile == null || this.profile.getDuration() == 0)
        {
            max = Aperture.duration.get();

            this.scale.lock(min, max);
            this.scale.view(min, max);
        }
        else
        {
            this.scale.lock(min, max);
        }

        this.clampScroll();
    }

    /* Public API methods  */

    /**
     * Set profile and update values which depends on camera profile
     */
    public void setProfile(CameraProfile profile)
    {
        boolean same = this.profile == profile;

        this.profile = profile;
        this.index = -1;

        this.set(0, Math.max(profile == null ? 0 : (int) profile.getDuration(), this.editor.maxScrub));
        this.value = MathHelper.clamp(this.value, this.min, this.max);

        long duration = profile == null ? 0 : profile.getDuration();

        if (!same && duration > 0 && this.area.w != 0)
        {
            this.scale.view(this.min, duration);
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
            this.scale.shiftInto(this.value);
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
    public int fromGraphX(int mouseX)
    {
        return (int) this.scale.from(mouseX);
    }

    /**
     * Calculate mouse X from given value
     */
    public int toGraphX(int value)
    {
        return (int) this.scale.to(value);
    }

    /**
     * Clamp the scroll value 
     */
    public void clampScroll()
    {
        this.scale.setShift(this.scale.getShift());
        this.scale.calculateMultiplier();
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
                this.setValueFromScrub(this.fromGraphX(mouseX));
            }
            else if (context.mouseButton == 1 && this.profile != null)
            {
                int tick = this.fromGraphX(mouseX);

                if (this.editor.creating)
                {
                    this.editor.addMarker(tick);

                    return false;
                }

                AbstractFixture fixture = this.profile.atTick(tick);
                long offset = this.profile.calculateOffset(fixture);

                if (fixture != null)
                {
                    boolean left = Math.abs(this.toGraphX((int) offset) - mouseX) < 5;
                    boolean right = Math.abs(this.toGraphX((int) (offset + fixture.getDuration())) - mouseX) < 5;

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
                if (!this.dragging)
                {
                    int index = this.profile.getAll().indexOf(fixture);

                    this.editor.pickCameraFixture(fixture, tick - offset);
                    this.index = index;
                }
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
            this.scale.zoom(Math.copySign(this.scale.getZoomFactor(), scroll), 0.001D, 1000D);

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
            this.setValueFromScrub(this.fromGraphX(mouseX));
        }

        if (this.scrolling)
        {
            this.scale.setShift(this.scale.getShift() - (mouseX - this.lastX) / this.scale.getZoom());
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

            long value = this.fromGraphX(mouseX);

            if (value >= start + 5 && (this.end == null || value <= end - 5))
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

                this.editor.updateDuration(this.end);
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
            int tx = this.toGraphX(this.value);
            int width = this.font.getStringWidth(label) + 4;

            /* Draw fixtures */
            int pos = 0;
            int i = 0;
            boolean drawnMarker = false;
            int leftMarginMarker = 0;
            int rightMarginMarker = 0;

            GuiDraw.scissor(x + 2, y - 16, w - 4, h + 16, context);

            this.drawTickMarks(y, h);

            for (AbstractFixture fixture : this.profile.getAll())
            {
                COLOR.set(fixture.getColor(), false);

                int color = COLOR.getRGBColor();

                if (color == 0)
                {
                    color = FixtureRegistry.CLIENT.get(fixture.getClass()).color.getRGBColor();
                }

                boolean selected = i == this.index;
                int leftMargin = this.toGraphX(pos) - 1;
                int rightMargin = this.toGraphX(pos + (int) fixture.getDuration()) - 1;

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
                if (fixture instanceof ManualFixture)
                {
                    ManualFixture manual = (ManualFixture) fixture;
                    int startTick = manual.shift;
                    int endTick = manual.getEndTick();
                    int start = MathUtils.clamp(leftMargin + (int) (startTick / (float) fixture.getDuration() * (rightMargin - leftMargin)), leftMargin, rightMargin);
                    int end = MathUtils.clamp(leftMargin + (int) (endTick / (float) fixture.getDuration() * (rightMargin - leftMargin)), leftMargin, rightMargin);

                    if (end < rightMargin)
                    {
                        Gui.drawRect(end + 1, y + (selected ? 12 : 15), rightMargin, y + h - 1, selected ? 0xaa000000 : 0x66000000);
                    }

                    if (end > leftMargin)
                    {
                        Gui.drawRect(start + 1, y + (selected ? 12 : 15), end + 1, y + h - 1, (selected ? 0xdd000000 : 0x66000000) + color);
                    }

                    if (start > leftMargin)
                    {
                        Gui.drawRect(leftMargin + 1, y + (selected ? 12 : 15), start + 1, y + h - 1, selected ? 0xaa000000 : 0x66000000);
                    }
                }
                else
                {
                    Gui.drawRect(leftMargin + 1, y + (selected ? 12 : 15), rightMargin, y + h - 1, (selected ? 0xdd000000 : 0x66000000) + color);
                }

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
                    COLOR.set(color, false);
                    COLOR.r *= 0.89F;
                    COLOR.g *= 0.89F;
                    COLOR.b *= 0.89F;

                    PathFixture path = (PathFixture) fixture;
                    int c = path.getCount() - 1;
                    int highlight = COLOR.getRGBAColor();

                    if (c > 1)
                    {
                        float fract = (rightMargin - leftMargin) / (float) c;

                        for (int j = 1; j < c; j++)
                        {
                            int px = leftMargin + (int) (fract * j);

                            Gui.drawRect(px, y + 5, px + 1, y + h - 1, highlight);
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
                    int mx = this.toGraphX(marker);

                    Gui.drawRect(mx, y + 5, mx + 1, y + h - 1,0xaaff0000);
                }
            }

            /* Draw shadows to indicate that there are still stuff to scroll */
            final double bias = 0.001;

            if (this.scale.getMinValue() > this.min + bias)
            {
                GuiDraw.drawHorizontalGradientRect(x + 2, y + h - 5, x + 22, y + h, 0x88000000, 0x00000000, 0);
            }

            if (this.scale.getMaxValue() < this.max - bias)
            {
                GuiDraw.drawHorizontalGradientRect(x + w - 22, y + h - 5, x + w - 2, y + h, 0x00000000, 0x88000000, 0);
            }

            /* Draw end marker and also shadow of area where there is no  */
            int stopX = this.toGraphX((int) this.profile.getDuration());

            if (stopX < this.area.ex() - 2)
            {
                this.drawGradientRect(stopX + 1, y + h / 2, x + w - 1, y + h, 0x00, 0x88000000);
                Gui.drawRect(stopX, y + h / 2, stopX + 1, y + h, 0xaaffffff);
            }

            if (tx + 3 - x + width > w)
            {
                tx -= 2;
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

    private void drawTickMarks(int y, int h)
    {
        int mult = this.scale.getMult() * 2;
        int start = (int) this.scale.getMinValue();
        int end = (int) this.scale.getMaxValue();
        int max = this.max - (this.max - 1) % mult;

        start -= start % mult;
        end -= end % mult;

        start = MathUtils.clamp(start, mult, max);
        end = MathUtils.clamp(end, mult, max);

        for (int j = start; j <= end; j += mult)
        {
            int xx = this.toGraphX(j);
            String value = String.valueOf(j);

            GuiDraw.drawTextBackground(this.font, value, xx - this.font.getStringWidth(value) / 2, y, 0xffffff, 0x88000000, 2);
            Gui.drawRect(xx, y + h / 2, xx + 1, y + h, 0x66ffffff);
        }
    }
}