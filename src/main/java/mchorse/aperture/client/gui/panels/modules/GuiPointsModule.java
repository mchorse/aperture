package mchorse.aperture.client.gui.panels.modules;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;

/**
 * Points GUI module
 *
 * This module is responsible for displaying "buttons" for picking up path
 * fixture's points, and also an ability to add or remove them.
 *
 * TODO: Add mouse scrolling
 */
public class GuiPointsModule implements IGuiModule
{
    /* Input */
    public PathFixture path;
    public IPointPicker picker;

    /* GUI */
    public Area area = new Area();
    public FontRenderer font;
    public GuiScreen screen;

    /* Scrolling variables */
    private int timer;
    private boolean dragging;
    private float scroll;
    private int scrollSize;
    private int lastY;

    /**
     * Currently selected button (shouldn't be deselected, i.e. can't be -1)
     */
    public int index = 0;

    public GuiPointsModule(IPointPicker picker, FontRenderer font)
    {
        this.picker = picker;
        this.font = font;
    }

    /**
     * Setup the path fixture and also fill or reset this module's fields based
     * on the path fixture.
     */
    public void fill(PathFixture path)
    {
        this.path = path;
        this.index = 0;
        this.scroll = 0;
        this.scrollSize = path.getCount() * 20;
    }

    public void update(GuiScreen screen, int x, int y, int h)
    {
        this.screen = screen;
        this.area.set(x, y, 20, h);
    }

    /**
     * Mouse was clicked
     *
     * This method responsible for adding and removing points in the path
     * fixture and initiating scrolling.
     */
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.area.isInside(mouseX, mouseY))
        {
            int dy = mouseY - this.area.y;
            int size = this.path.getCount();

            if (dy < 20)
            {
                /* Add a point based on player attributes */
                this.path.addPoint(new Position(Minecraft.getMinecraft().thePlayer), this.index + 1);
                this.scrollSize = this.path.getCount() * 20;
            }
            else if (dy > this.area.h - 20 && size > 1)
            {
                /* Remove a point and update scroll */
                this.path.removePoint(this.index);
                this.scrollSize = (size - 1) * 20;

                if (this.scrollSize > this.area.h - 40)
                {
                    this.scroll = MathHelper.clamp_float(this.scroll, 0, this.scrollSize - (this.area.h - 40));
                }
                else
                {
                    this.scroll = 0.0F;
                }
            }
            else
            {
                /* Initiate dragging */
                this.dragging = true;
                this.lastY = mouseY;
                this.timer = 0;
            }
        }
    }

    /**
     * Mouse button was released
     *
     * If scrolling was initiated on click, this method will be responsible for
     * selecting a point in the path or shifting the playback scrub to the
     * location of the of current path point.
     */
    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (this.dragging && this.timer < 6)
        {
            int dy = mouseY - this.area.y - 20 + (int) this.scroll;
            int index = dy / 20;
            int size = this.path.getCount();

            if (index == this.index)
            {
                /* Go to the path point */
                GuiCameraEditor editor = (GuiCameraEditor) this.screen;
                int pos = (int) editor.getProfile().calculateOffset(this.path);
                int increment = (int) ((this.index / (float) (size - 1)) * this.path.getDuration());

                if (increment > 0)
                {
                    increment -= 1;
                }

                editor.scrub.setValue(pos + increment);
            }
            else if (index >= 0 && index < size)
            {
                /* Pick a point */
                this.index = index;

                if (this.picker != null)
                {
                    this.picker.pickPoint(this, index);
                }
            }
        }

        this.dragging = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {}

    /**
     * Draw the module
     *
     * This method will draw the background, button labels (+/-) and also alls
     * the buttons. It also responsible for scrolling.
     */
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        /* Scroll this view */
        if (this.dragging)
        {
            if (this.scrollSize > this.area.h - 40)
            {
                this.scroll += this.lastY - mouseY;
                this.scroll = MathHelper.clamp_float(this.scroll, 0, this.scrollSize - (this.area.h - 40));

                this.lastY = mouseY;
            }

            this.timer++;
        }

        int x = this.area.x;
        int y = this.area.y;
        int c = this.path.getCount();

        /* Draw background and buttons */
        Gui.drawRect(x, y, x + this.area.w, y + this.area.h, 0x88000000);
        GuiUtils.scissor(this.area.x, this.area.y + 20, 20, this.area.h - 40, this.screen.width, this.screen.height);

        for (int i = 0; i < c; i++)
        {
            String label = String.valueOf(i);
            int yy = this.area.y + 20 + i * 20 - (int) this.scroll;
            int w = this.font.getStringWidth(label);

            Gui.drawRect(x, yy, x + 20, yy + 20, this.index == i ? 0xffdd2280 : 0xffff2280);
            Gui.drawRect(x, yy + 19, x + 20, yy + 20, 0x22000000);
            this.font.drawStringWithShadow(label, x + 10 - w / 2, yy + 6, 0xffffff);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (this.scroll > 0 && this.scrollSize >= this.area.h - 40)
        {
            Gui.drawRect(x, y + 20, x + this.area.w, y + 22, 0x88000000);
        }

        if (this.scroll < this.scrollSize - (this.area.h - 40) && this.scrollSize >= this.area.h - 40)
        {
            Gui.drawRect(x, y + this.area.h - 22, x + this.area.w, y + this.area.h - 20, 0x88000000);
        }

        /* Draw add and remove buttons */
        this.font.drawStringWithShadow("+", x + 7, y + 7, 0xffffff);
        this.font.drawStringWithShadow("-", x + 7, y + this.area.h - 13, 0xffffff);
    }

    /**
     * Point picker interface
     */
    public static interface IPointPicker
    {
        public void pickPoint(GuiPointsModule module, int index);
    }
}