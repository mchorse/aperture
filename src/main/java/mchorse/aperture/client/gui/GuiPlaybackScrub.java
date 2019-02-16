package mchorse.aperture.client.gui;

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
 * GUI playback scrub
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
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.clampScroll();
    }

    /* Public API methods  */

    /**
     * Set profile and update values which depends on camera profile
     */
    public void setProfile(CameraProfile profile)
    {
        this.profile = profile;
        this.index = -1;

        this.max = profile == null ? 0 : (int) profile.getDuration();
        this.value = MathHelper.clamp_int(this.value, this.min, this.max);
        this.scroll = 0;
        this.scale = 1;
    }

    /**
     * Set the value of the scrubber. Also, if the value has changed notify
     * the listener.
     */
    public void setValue(int value, boolean fromScrub)
    {
        int old = this.value;

        this.value = value;
        this.value = MathHelper.clamp_int(this.value, this.min, this.max);

        if (this.value != old)
        {
            this.editor.scrubbed(this, this.value, fromScrub);
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
     * Set the value of the scrubber from scrub
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
        float factor = (mouseX - 2 - this.area.x + this.scroll) / (this.max * this.scale);

        return (int) (factor * (this.max - this.min)) + this.min;
    }

    /**
     * Calculate mouse X from given value
     */
    public int calcMouseFromValue(int value)
    {
        float factor = (value - this.min) / (float) (this.max - this.min);

        return (int) (factor * this.max * this.scale) + this.area.x + 2 - this.scroll;
    }

    /**
     * Clamp the scroll value 
     */
    public void clampScroll()
    {
        int max = (int) (this.max * this.scale) - this.area.w + 4;

        this.scroll = MathHelper.clamp_int(this.scroll, 0, max > 0 ? max : 0);
    }

    /* GUI interactions */

    /**
     * Mouse was clicked
     */
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.area.isInside(mouseX, mouseY))
        {
            if (mouseButton == 0)
            {
                this.scrubbing = true;
                this.setValueFromScrub(this.calcValueFromMouse(mouseX));

                return true;
            }
            else if (mouseButton == 1 && this.profile != null)
            {
                int tick = this.calcValueFromMouse(mouseX);
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

                return true;
            }
            else if (mouseButton == 2)
            {
                this.scrolling = true;
                this.lastX = mouseX;

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, int scroll)
    {
        if (!Minecraft.IS_RUNNING_ON_MAC)
        {
            scroll = -scroll;
        }

        if (this.area.isInside(mouseX, mouseY) && !this.scrolling)
        {
            float scale = this.scale;
            int value = (int) (this.calcValueFromMouse(mouseX) * scale);

            this.scale += Math.copySign(0.1F, scroll);
            this.scale = MathHelper.clamp_float(this.scale, 0.1F, 10F);

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

        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    /**
     * Mouse was released
     */
    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
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
     * Draw scrub on the screen
     *
     * This scrub looks quite simple. The line part is inspired by Blender's
     * timeline thingy. Scrub also renders all of available camera fixtures.
     */
    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        if (this.scrubbing)
        {
            this.setValueFromScrub(this.calcValueFromMouse(mouseX));

            if (this.max * this.scale - this.area.w + 4 > 0)
            {
                int delta = mouseX - this.area.getX(0.5F);
                int edge = (int) Math.copySign(this.area.w / 2 - 50, -delta) + delta;

                if (Math.copySign(1, edge) == Math.copySign(1, delta))
                {
                    float factor = edge / 50F;

                    this.scroll += factor * 5 * this.scale;
                    this.clampScroll();
                }
            }
        }

        if (this.scrolling)
        {
            this.scroll -= (mouseX - this.lastX) * this.scale;
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

        /* Calculate tick marker position and tick label width */
        String label = String.valueOf(this.value + "/" + this.max);
        float f = (float) (this.value - this.min) / (float) (this.max - this.min);
        int tx = x + 2 + (int) (this.max * f * this.scale) - this.scroll;
        int width = this.font.getStringWidth(label) + 4;

        /* Draw fixtures */
        int pos = 0;
        int i = 0;
        boolean drawnMarker = false;
        int leftMarginMarker = 0;
        int rightMarginMarker = 0;

        GuiUtils.scissor(x + 2, y - 16, w - 4, h + 16, this.editor.width, this.editor.height);

        for (AbstractFixture fixture : this.profile.getAll())
        {
            int color = FixtureRegistry.CLIENT.get(fixture.getClass()).color.getHex();

            boolean selected = i == this.index;
            float leftFactor = (float) (pos - this.min) / (float) (this.max - this.min);
            float rightFactor = (float) (pos + fixture.getDuration() - this.min) / (float) (this.max - this.min);
            int leftMargin = x + 1 + (int) (this.max * leftFactor * this.scale) - this.scroll;
            int rightMargin = x + 1 + (int) (this.max * rightFactor * this.scale) - this.scroll;

            /* Draw fixture's background and the hinge */
            Gui.drawRect(leftMargin + 1, y + 15, rightMargin, y + h - 1, (selected ? 0xff000000 : 0x66000000) + color);
            Gui.drawRect(rightMargin, y + 1, rightMargin + 1, y + h - 1, 0xff000000 + color);

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
                        int fract = (rightMargin - leftMargin) / c;

                        for (int j = 1; j < c; j++)
                        {
                            int px = leftMargin + fract * j;

                            Gui.drawRect(px, y + 5, px + 1, y + h - 1, 0xff000000 + color - 0x00181818);
                        }
                    }
                }
            }

            if (this.area.isInside(mouseX, mouseY) && !this.resize && !drawnMarker)
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

                if (lw + 4 < rightMargin - leftMargin)
                {
                    this.font.drawStringWithShadow(name, leftMargin + 4, y + 6, textColor);
                }
                else
                {
                    this.font.drawStringWithShadow("...", leftMargin + 4, y + 6, textColor);
                }
            }

            pos += fixture.getDuration();
            i++;
        }

        if (this.scroll > 0) GuiUtils.drawHorizontalGradientRect(x + 2, y + h - 5, x + 22, y + h, 0x88000000, 0x00000000, 0);
        if (this.scroll < this.max * this.scale - this.area.w + 4) GuiUtils.drawHorizontalGradientRect(x + w - 22, y + h - 5, x + w - 2, y + h, 0x00000000, 0x88000000, 0);

        /* Draw the marker */
        Gui.drawRect(tx, y + 1, tx + 2, y + h - 1, 0xff57f52a);

        /* Draw the "how far into fixture" tick */
        String offsetLabel = String.valueOf(this.value - this.profile.calculateOffset(this.value, false));
        int ow = this.font.getStringWidth(offsetLabel);

        this.font.drawStringWithShadow(offsetLabel, tx - ow / 2 + 1, y + h - this.font.FONT_HEIGHT * 3 - 1, 0xffffff);

        /* Move the tick line left, so it won't overflow the scrub */
        if (tx + 3 - x + width > w)
        {
            tx -= width + 2;
        }

        /* Draw the tick label */
        Gui.drawRect(tx + 2, y + h - 3 - this.font.FONT_HEIGHT, tx + 2 + width, y + h - 1, 0xff57f52a);
        this.font.drawStringWithShadow(label, tx + 4, y + h - this.font.FONT_HEIGHT - 1, 0xffffff);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        /* Draw resizing markers */
        if (drawnMarker)
        {
            int markerOffset = (Math.abs(leftMarginMarker - mouseX) < 5 ? leftMarginMarker : rightMarginMarker);

            Gui.drawRect(markerOffset - 4, this.area.y - 1, markerOffset + 5, this.area.y, 0xaaffffff);
            Gui.drawRect(markerOffset - 5, this.area.y - 1 - 2, markerOffset - 4, this.area.y + 2, 0xaaffffff);
            Gui.drawRect(markerOffset + 5, this.area.y - 1 - 2, markerOffset + 6, this.area.y + 2, 0xaaffffff);
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