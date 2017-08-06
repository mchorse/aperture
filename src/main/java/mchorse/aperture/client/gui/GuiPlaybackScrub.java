package mchorse.aperture.client.gui;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraRenderer;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * GUI playback scrub
 *
 * This class is responsible for rendering and controlling the playback
 */
public class GuiPlaybackScrub
{
    /**
     * Vanilla buttons resource location
     */
    public static final ResourceLocation VANILLA_BUTTONS = new ResourceLocation("textures/gui/widgets.png");

    /* Box of the scrub */
    public Area area = new Area();

    public boolean scrubbing;
    public int value;
    public int min;
    public int max;
    public int index;

    public IScrubListener listener;
    public CameraProfile profile;
    public FontRenderer font;

    public GuiPlaybackScrub(IScrubListener listener, CameraProfile profile)
    {
        this.listener = listener;
        this.profile = profile;
        this.font = Minecraft.getMinecraft().fontRendererObj;
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
        this.value = MathHelper.clamp(this.value, this.min, this.max);
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

        if (this.value != old && this.listener != null)
        {
            this.listener.scrubbed(this, this.value, fromScrub);
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
        float factor = (float) (mouseX + 1 - this.area.x) / (float) this.area.w;

        return (int) (factor * (this.max - this.min)) + this.min;
    }

    /* GUI interactions */

    /**
     * Mouse was clicked
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.area.isInside(mouseX, mouseY))
        {
            if (mouseButton == 0)
            {
                this.scrubbing = true;
                this.setValueFromScrub(this.calcValueFromMouse(mouseX));
            }
            else if (mouseButton == 1 && this.profile != null)
            {
                /* Select camera fixture */
                int tick = this.calcValueFromMouse(mouseX);
                GuiCameraEditor editor = (GuiCameraEditor) this.listener;
                AbstractFixture fixture = this.profile.atTick(tick);
                int index = this.profile.getAll().indexOf(fixture);

                editor.pickCameraFixture(fixture, tick - this.profile.calculateOffset(fixture));
                this.index = index;
            }
        }
    }

    /**
     * Mouse was released
     */
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.scrubbing = false;
    }

    /**
     * Draw scrub on the screen
     *
     * This scrub looks quite simple. The line part is inspired by Blender's
     * timeline thingy. Scrub also renders all of available camera fixtures.
     */
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        if (this.scrubbing)
        {
            this.setValueFromScrub(this.calcValueFromMouse(mouseX));
        }

        int x = this.area.x;
        int y = this.area.y;
        int w = this.area.w;
        int h = this.area.h;

        /* Draw background */
        Gui.drawRect(x, y + h - 1, x + w, y + h, 0xffffffff);

        /* Calculate tick marker position and tick label width */
        String label = String.valueOf(this.value + "/" + this.max);
        float f = (float) (this.value - this.min) / (float) (this.max - this.min);
        int tx = x + 1 + (int) ((w - 4) * f);
        int width = this.font.getStringWidth(label) + 4;

        /* Draw fixtures */
        int pos = 0;
        int i = 0;

        for (AbstractFixture fixture : this.profile.getAll())
        {
            CameraRenderer.Color color = CameraRenderer.fromFixture(fixture);

            boolean selected = i == this.index;
            float ff = (float) (pos + fixture.getDuration() - this.min) / (float) (this.max - this.min);
            float fb = (float) (pos - this.min) / (float) (this.max - this.min);
            int fx = x + 1 + (int) ((w - 3) * ff);
            int fbx = x + 1 + (int) ((w - 3) * fb);

            Gui.drawRect(fbx + 1, y + 15, fx, y + h - 1, (selected ? 0xff000000 : 0x66000000) + color.hex);
            Gui.drawRect(fx, y + 1, fx + 1, y + h - 1, 0xff000000 + color.hex);

            String name = fixture.getName();

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
                            int fract = (int) ((fx - fbx) * ((float) frame / duration));
                            int px = fbx + fract;

                            Gui.drawRect(px, y + 5, px + 1, y + h - 1, 0xff000000 + color.hex - 0x00181818);

                            frame += path.getPoint(j).getDuration();
                        }
                    }
                    else
                    {
                        int fract = (fx - fbx) / c;

                        for (int j = 1; j < c; j++)
                        {
                            int px = fbx + fract * j;

                            Gui.drawRect(px, y + 5, px + 1, y + h - 1, 0xff000000 + color.hex - 0x00181818);
                        }
                    }
                }
            }

            if (!name.isEmpty())
            {
                int lw = this.font.getStringWidth(name);
                int textColor = selected ? 16777120 : 0xffffff;

                if (lw + 4 < fx - fbx)
                {
                    this.font.drawStringWithShadow(name, fbx + 4, y + 6, textColor);
                }
                else
                {
                    this.font.drawStringWithShadow("...", fbx + 4, y + 6, textColor);
                }
            }

            pos += fixture.getDuration();
            i++;
        }

        /* Draw the marker */
        Gui.drawRect(tx, y + 1, tx + 2, y + h - 1, 0xff57f52a);

        /* Move the tick line left, so it won't overflow the scrub */
        if (tx + 3 - x + width > w)
        {
            tx -= width + 2;
        }

        /* Draw the tick label */
        Gui.drawRect(tx + 2, y + h - 3 - this.font.FONT_HEIGHT, tx + 2 + width, y + h - 1, 0xff57f52a);
        this.font.drawStringWithShadow(label, tx + 4, y + h - this.font.FONT_HEIGHT - 1, 0xffffff);
    }

    /**
     * Scrub event listener
     */
    public static interface IScrubListener
    {
        public void scrubbed(GuiPlaybackScrub scrub, int value, boolean fromScrub);
    }
}