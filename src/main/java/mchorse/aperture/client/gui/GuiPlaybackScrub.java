package mchorse.aperture.client.gui;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraRenderer;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.utils.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiUtils;

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
    public Rect area = new Rect();

    public boolean scrubbing;
    public int value;
    public int min;
    public int max;

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
        this.max = (int) profile.getDuration();
        this.value = MathHelper.clamp_int(this.value, this.min, this.max);
    }

    /**
     * Set the value of the scrubber. Also, if the value has changed notify
     * the listener.
     */
    public void setValue(int value)
    {
        int old = this.value;

        this.value = value;
        this.value = MathHelper.clamp_int(this.value, this.min, this.max);

        if (this.value != old && this.listener != null)
        {
            this.listener.scrubbed(this, this.value);
        }
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
            this.scrubbing = true;
            this.setValue(this.calcValueFromMouse(mouseX));
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
            this.setValue(this.calcValueFromMouse(mouseX));
        }

        int x = this.area.x;
        int y = this.area.y;
        int w = this.area.w;
        int h = this.area.h;

        /* Draw background */
        GuiUtils.drawContinuousTexturedBox(VANILLA_BUTTONS, x, y, 0, 46, w, h, 200, 20, 1, 1, 1, 1, 0);

        /* Calculate tick marker position and tick label width */
        String label = String.valueOf(this.value);
        float f = (float) (this.value - this.min) / (float) (this.max - this.min);
        int tx = x + 1 + (int) ((w - 4) * f);
        int width = this.font.getStringWidth(label) + 4;

        /* Draw fixtures */
        int pos = 0;

        for (AbstractFixture fixture : this.profile.getAll())
        {
            CameraRenderer.Color color = CameraRenderer.fromFixture(fixture);

            float ff = (float) (pos + fixture.getDuration() - this.min) / (float) (this.max - this.min);
            float fb = (float) (pos - this.min) / (float) (this.max - this.min);
            int fx = x + 1 + (int) ((w - 3) * ff);
            int fbx = x + 1 + (int) ((w - 3) * fb);

            Gui.drawRect(fbx + 1, y + 2, fx, y + h - 2, 0x44000000 + color.hex);
            Gui.drawRect(fx, y + 1, fx + 1, y + h - 1, 0xff000000 + color.hex);

            String name = fixture.getName();

            if (!name.isEmpty())
            {
                int lw = this.font.getStringWidth(name);

                if (lw + 4 < fx - fbx)
                {
                    this.font.drawStringWithShadow(name, fbx + 4, y + 7, 0xffffff);
                }
                else
                {
                    this.font.drawStringWithShadow("...", fbx + 4, y + 7, 0xffffff);
                }
            }

            pos += fixture.getDuration();
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
        public void scrubbed(GuiPlaybackScrub scrub, int value);
    }
}