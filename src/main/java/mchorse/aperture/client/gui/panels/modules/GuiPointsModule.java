package mchorse.aperture.client.gui.panels.modules;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.DurablePosition;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.client.gui.panels.IButtonListener;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.client.gui.widgets.GuiButtonList;
import mchorse.aperture.client.gui.widgets.buttons.GuiTextureButton;
import mchorse.aperture.utils.ScrollArea;
import mchorse.aperture.utils.ScrollArea.ScrollDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * Points GUI module
 *
 * This module is responsible for displaying "buttons" for picking up path
 * fixture's points, and also an ability to add or remove them.
 */
public class GuiPointsModule implements IGuiModule, IButtonListener
{
    /* Input */
    public PathFixture path;
    public GuiPathFixturePanel picker;

    /* GUI */
    public ScrollArea area = new ScrollArea(20);
    public FontRenderer font;
    public GuiScreen screen;
    public GuiButtonList buttons;

    /* Scrolling variables */
    private boolean dragging;
    private int lastY;

    /**
     * Currently selected button (shouldn't be deselected, i.e. can't be -1)
     */
    public int index = 0;

    public GuiPointsModule(GuiPathFixturePanel picker, FontRenderer font)
    {
        this.picker = picker;
        this.font = font;

        this.buttons = new GuiButtonList(Minecraft.getMinecraft(), this);
        this.buttons.add(new GuiTextureButton(0, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(160, 0).setActiveTexPos(160, 16));
        this.buttons.add(new GuiTextureButton(1, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(224, 0).setActiveTexPos(224, 16));

        this.buttons.add(new GuiTextureButton(2, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(240, 0).setActiveTexPos(240, 16));
        this.buttons.add(new GuiTextureButton(3, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(144, 0).setActiveTexPos(144, 16));

        this.area.direction = ScrollDirection.HORIZONTAL;
    }

    @Override
    public void actionButtonPerformed(GuiButton button)
    {
        int size = this.path.getCount();

        if (button.id == 0 && this.index > 0)
        {
            /* Move point backward */
            this.path.movePoint(this.index, this.index - 1);
            this.index--;
        }
        else if (button.id == 1)
        {
            /* Add a point based on player attributes */
            this.path.addPoint(new DurablePosition(Minecraft.getMinecraft().player), this.index + 1);

            this.index++;
            this.area.setSize(this.path.getCount());
            this.area.scrollTo((int) (this.index / (float) this.path.getCount() * this.area.scrollSize));

            if (this.picker != null)
            {
                this.picker.pickPoint(this, this.index);
            }
        }
        else if (button.id == 2 && size > 1)
        {
            /* Remove a point and update scroll */
            this.path.removePoint(this.index);

            if (this.index > 0)
            {
                this.index--;
            }

            this.area.setSize(this.path.getCount());
            this.area.scrollTo((int) (this.index / (float) this.path.getCount() * this.area.scrollSize));

            if (this.picker != null)
            {
                this.picker.pickPoint(this, this.index);
            }
        }
        else if (button.id == 3 && this.index < size - 1)
        {
            /* Move point forward */
            this.path.movePoint(this.index, this.index + 1);
            this.index++;
        }

        this.picker.editor.getProfile().dirty();
    }

    /**
     * Setup the path fixture and also fill or reset this module's fields based
     * on the path fixture.
     */
    public void fill(PathFixture path)
    {
        this.path = path;
        this.index = 0;
        this.area.scrollTo(0);
        this.area.setSize(path.getCount());
    }

    public void update(GuiScreen screen, int x, int y, int w, int h)
    {
        this.screen = screen;
        this.area.set(x, y, w, h);

        GuiUtils.setSize(this.buttons.buttons.get(0), x - 38, y + 2, 16, 16);
        GuiUtils.setSize(this.buttons.buttons.get(2), x - 18, y + 2, 16, 16);

        GuiUtils.setSize(this.buttons.buttons.get(1), x + w + 2, y + 2, 16, 16);
        GuiUtils.setSize(this.buttons.buttons.get(3), x + w + 22, y + 2, 16, 16);
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
            if (mouseButton == 1)
            {
                /* Initiate dragging */
                this.dragging = true;
                this.lastY = mouseX;
            }
            else if (mouseButton == 0)
            {
                int index = this.area.getIndex(mouseX, mouseY);
                int size = this.path.getCount();

                if (index >= 0 && index < size)
                {
                    /* Pick a point */
                    this.index = index;

                    if (this.picker != null)
                    {
                        this.picker.pickPoint(this, index);
                    }
                }
            }
        }

        this.buttons.mouseClicked(mouseX, mouseY, mouseButton);
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
        if (this.dragging && this.area.scrollSize > this.area.w)
        {
            this.area.scrollBy(this.lastY - mouseX);

            this.lastY = mouseX;
        }

        int x = this.area.x;
        int y = this.area.y;
        int c = this.path.getCount();

        /* Draw background and buttons */
        Gui.drawRect(x, y, x + this.area.w, y + this.area.h, 0x88000000);
        GuiUtils.scissor(this.area.x, this.area.y, this.area.w, this.area.h, this.screen.width, this.screen.height);

        for (int i = 0; i < c; i++)
        {
            String label = String.valueOf(i);
            int xx = this.area.x + i * this.area.scrollItemSize - (int) this.area.scroll;
            int w = this.font.getStringWidth(label);

            Gui.drawRect(xx, y, xx + 20, y + 20, this.index == i ? 0xffdd2280 : 0xffff2280);
            Gui.drawRect(xx + 19, y, xx + 20, y + 20, 0x22000000);
            this.font.drawStringWithShadow(label, xx + 10 - w / 2, y + 6, 0xffffff);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        /* Display scroll bar */
        int mw = this.area.w;
        int scroll = this.area.getScrollBar(mw);

        if (scroll != 0)
        {
            int bx = this.area.x + (int) (this.area.scroll / (float) (this.area.scrollSize - this.area.w) * (mw - scroll));
            int by = y + this.area.h + 2;

            Gui.drawRect(bx, by, bx + scroll, by + 2, 0x88000000);
        }

        /* Overlay "shadows" for informing the user that  */
        if (this.area.scroll > 0 && this.area.scrollSize >= this.area.w - 40)
        {
            GuiUtils.drawHorizontalGradientRect(x, y, x + 4, y + this.area.h, 0x88000000, 0x0, 0);
        }

        if (this.area.scroll < this.area.scrollSize - this.area.w && this.area.scrollSize >= this.area.w)
        {
            GuiUtils.drawHorizontalGradientRect(x + this.area.w - 4, y, x + this.area.w, y + this.area.h, 0x0, 0x88000000, 0);
        }

        this.buttons.draw(mouseX, mouseY);

        String label = I18n.format("aperture.gui.panels.path_points");
        int w = this.font.getStringWidth(label);
        this.font.drawStringWithShadow(label, this.area.x + this.area.w / 2 - w / 2, this.area.y - 14, 0xffffff);
    }

    /**
     * Point picker interface
     */
    public static interface IPointPicker
    {
        public void pickPoint(GuiPointsModule module, int index);
    }
}