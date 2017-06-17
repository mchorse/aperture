package mchorse.aperture.client.gui;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraRenderer;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.utils.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.math.MathHelper;

/**
 * Camera fixtures GUI
 *
 * This class is responsible for displaying available camera fixtures from
 * camera profile, scrolling through them, and picking them for editing.
 */
public class GuiCameraFixtures
{
    private int timer;
    private boolean dragging;

    private float scroll;
    private int scrollSize;
    private int prevScroll;
    private int lastX;
    private float acc;

    private int index = -1;

    public CameraProfile profile;
    public IFixturePicker picker;
    public Rect area = new Rect();
    public FontRenderer font;
    public GuiCameraEditor editor;

    public GuiCameraFixtures(IFixturePicker picker, CameraProfile profile)
    {
        this.picker = picker;
        this.profile = profile;
        this.editor = (GuiCameraEditor) picker;
        this.font = Minecraft.getMinecraft().fontRendererObj;
    }

    /**
     * Set camera profile
     */
    public void setProfile(CameraProfile profile)
    {
        this.profile = profile;

        if (this.profile != null)
        {
            this.updateScroll();
            this.index = -1;
        }
    }

    /**
     * Update scroll information
     */
    public void updateScroll()
    {
        if (this.profile != null)
        {
            this.scrollSize = this.area.h * this.profile.getCount();
            this.scroll = this.scrollSize > this.area.w ? MathHelper.clamp_float(this.scroll, 0, this.scrollSize - this.area.w) : 0;
        }
    }

    public int getIndex()
    {
        return this.index;
    }

    public void decrementIndex()
    {
        this.index--;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * This method is responsible for initiating dragging sequence, if the click
     * happened inside of this widget's area
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.area.isInside(mouseX, mouseY))
        {
            this.timer = 0;
            this.acc = 0;
            this.dragging = true;
            this.prevScroll = this.lastX = mouseX;
        }
    }

    /**
     * Pick a camera fixture on click release (in case if it was a quite quick
     * click).
     */
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        /* Click happened, means go and pick the thing */
        if (this.area.isInside(mouseX, mouseY) && this.dragging)
        {
            if (this.timer < 8)
            {
                int index = (mouseX - this.area.x + (int) this.scroll) / this.area.h;

                if (index >= 0 && index < this.profile.getCount() && index != this.index)
                {
                    this.index = index;

                    if (this.picker != null)
                    {
                        this.picker.pickCameraFixture(this.profile.get(index));
                    }
                }
                else
                {
                    this.index = -1;

                    if (this.picker != null)
                    {
                        this.picker.pickCameraFixture(null);
                    }
                }
            }

            int diff = mouseX - this.prevScroll;
            float factor = diff == 0 ? 0.0F : 1.0F / 5.0F;

            this.acc = (this.lastX - mouseX) * factor;
        }

        this.dragging = false;
    }

    /**
     * Draw the camera fixtures
     */
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        /* Scrolling through the camera fixtures */
        if (this.dragging)
        {
            if (this.scrollSize > this.area.w)
            {
                this.scroll += this.prevScroll - mouseX;
                this.scroll = MathHelper.clamp_float(this.scroll, 0, this.scrollSize - this.area.w);

                this.prevScroll = mouseX;
            }

            this.timer++;
        }

        if (this.acc != 0 && this.scrollSize > this.area.w)
        {
            this.acc *= 0.94F;
            this.scroll += this.acc;
            this.scroll = MathHelper.clamp_float(this.scroll, 0, this.scrollSize - this.area.w);

            if (Math.abs(this.acc) < 0.0005F)
            {
                this.acc = 0.0F;
            }
        }

        GuiUtils.scissor(this.area.x, this.area.y - 2, this.area.w, this.area.h + 2, this.editor.width, this.editor.height);
        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0xaa000000);

        int i = 0;

        for (AbstractFixture fixture : this.profile.getAll())
        {
            if (i == this.index)
            {
                i++;

                continue;
            }

            int hex = 0xff000000 + CameraRenderer.fromFixture(fixture).hex;

            int s = this.area.h;
            int x = this.area.x + s * i - (int) this.scroll;
            int y = this.area.y;

            String label = String.valueOf(i);
            int w = this.font.getStringWidth(label);

            /* Background */
            Gui.drawRect(x, y, x + s, y + s, hex);

            /* Shadow on the right side */
            Gui.drawRect(x + s - 1, y, x + s, y + s, 0x33000000);
            this.font.drawStringWithShadow(label, x + s / 2 - w / 2, y + s / 2 - 4, 0xffffff);

            i++;
        }

        /* Global highlight and shadow */
        int x = this.area.x - (int) this.scroll;

        Gui.drawRect(x, this.area.y, x + this.area.h * i, this.area.y + 1, 0x22ffffff);
        Gui.drawRect(x, this.area.y + this.area.h - 1, x + this.area.h * i, this.area.y + this.area.h, 0x22000000);

        if (this.index != -1 && this.index >= 0 && this.index < i)
        {
            int j = this.index;
            int hex = 0xff000000 + CameraRenderer.fromFixture(this.profile.get(j)).hex;

            int s = this.area.h;
            int xx = this.area.x + s * j - (int) this.scroll;
            int y = this.area.y - 2;

            String label = String.valueOf(j);
            int w = this.font.getStringWidth(label);

            /* Background */
            Gui.drawRect(xx, y, xx + s, y + s + 2, hex);

            /* Shadow on the right side */
            Gui.drawRect(xx + s - 1, y, xx + s, y + s + 2, 0x33000000);
            this.font.drawStringWithShadow(label, xx + s / 2 - w / 2, y + s / 2 - 4, 0xffffff);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /**
     * Fixture picker
     */
    public static interface IFixturePicker
    {
        public void pickCameraFixture(AbstractFixture fixture);
    }
}