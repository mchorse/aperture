package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.DurablePosition;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.utils.APIcons;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.ScrollArea;
import mchorse.mclib.client.gui.utils.ScrollArea.ScrollDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

/**
 * Points GUI module
 *
 * This module is responsible for displaying "buttons" for picking up path
 * fixture's points, and also an ability to add or remove them.
 */
public class GuiPointsModule extends GuiAbstractModule
{
    /* Input */
    public PathFixture path;
    public GuiPathFixturePanel picker;

    /* GUI */
    public ScrollArea scroll = new ScrollArea(20);

    /**
     * Currently selected button (shouldn't be deselected, i.e. can't be -1)
     */
    public int index = 0;

    public GuiPointsModule(Minecraft mc, GuiCameraEditor editor, GuiPathFixturePanel picker)
    {
        super(mc, editor);

        this.picker = picker;

        GuiIconElement back = new GuiIconElement(mc, APIcons.MOVE_BACK, (b) ->
        {
            if (this.index == 0) return;

            this.path.movePoint(this.index, this.index - 1);
            this.index--;
            this.editor.updateProfile();
        });

        GuiIconElement add = new GuiIconElement(mc, Icons.ADD, (b) -> this.addPoint());
        GuiIconElement remove = new GuiIconElement(mc, Icons.REMOVE, (b) -> this.removePoint());

        GuiIconElement forward = new GuiIconElement(mc, APIcons.MOVE_FORWARD, (b) ->
        {
            if (this.index >= this.path.getCount() - 1) return;

            this.path.movePoint(this.index, this.index + 1);
            this.index++;
            this.editor.updateProfile();
        });

        back.flex().parent(this.area).set(-38, 2, 16, 16);
        remove.flex().parent(this.area).set(-18, 2, 16, 16);
        add.flex().parent(this.area).set(0, 2, 16, 16).x(1, 2);
        forward.flex().parent(this.area).set(0, 2, 16, 16).x(1, 22);

        this.add(back, add, remove, forward);
        this.scroll.direction = ScrollDirection.HORIZONTAL;
    }

    public void addPoint()
    {
        if (this.index + 1 == this.path.getPoints().size())
        {
            this.path.addPoint(new DurablePosition(this.editor.getPosition()));
        }
        else
        {
            this.path.addPoint(new DurablePosition(this.editor.getPosition()), this.index + 1);
        }

        this.index++;
        this.scroll.setSize(this.path.getCount());
        this.scroll.scrollTo((int) (this.index / (float) this.path.getCount() * this.scroll.scrollSize));

        if (this.picker != null)
        {
            this.picker.pickPoint(this, this.index);
        }

        this.editor.updateProfile();
    }

    public void removePoint()
    {
        if (this.path.getPoints().size() == 1)
        {
            return;
        }

        this.path.removePoint(this.index);

        if (this.index > 0)
        {
            this.index--;
        }

        this.scroll.setSize(this.path.getCount());
        this.scroll.scrollTo((int) (this.index / (float) this.path.getCount() * this.scroll.scrollSize));

        if (this.picker != null)
        {
            this.picker.pickPoint(this, this.index);
        }

        this.editor.updateProfile();
    }

    /**
     * Setup the path fixture and also fill or reset this module's fields based
     * on the path fixture.
     */
    public void fill(PathFixture path)
    {
        this.path = path;
        this.index = 0;
        this.scroll.setSize(path.getCount());
        this.scroll.clamp();
    }

    @Override
    public void resize()
    {
        super.resize();

        this.scroll.copy(this.area);
    }

    /**
     * Mouse was clicked
     *
     * This method responsible for adding and removing points in the path
     * fixture and initiating scrolling.
     */
    @Override
    public boolean mouseClicked(GuiContext context)
    {
        if (super.mouseClicked(context))
        {
            return true;
        }

        int mouseX = context.mouseX;
        int mouseY = context.mouseY;

        if (this.scroll.isInside(context))
        {
            if (context.mouseButton == 1)
            {
                this.scroll.dragging = true;

                return true;
            }
            else if (context.mouseButton == 0)
            {
                int index = this.scroll.getIndex(mouseX, mouseY);
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

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(GuiContext context)
    {
        return super.mouseScrolled(context) || this.scroll.mouseScroll(context);
    }

    /**
     * Mouse button was released
     *
     * If scrolling was initiated on click, this method will be responsible for
     * selecting a point in the path or shifting the playback scrub to the
     * location of the of current path point.
     */
    @Override
    public void mouseReleased(GuiContext context)
    {
        super.mouseReleased(context);
        this.scroll.mouseReleased(context);
    }

    /**
     * Draw the module
     *
     * This method will draw the background, button labels (+/-) and also alls
     * the buttons. It also responsible for scrolling.
     */
    @Override
    public void draw(GuiContext context)
    {
        /* Scroll this view */
        this.scroll.drag(context);

        int x = this.scroll.x;
        int y = this.scroll.y;
        int c = this.path.getCount();

        /* Draw background and buttons */
        Gui.drawRect(x, y, x + this.scroll.w, y + this.scroll.h, 0x88000000);
        GuiDraw.scissor(this.scroll.x, this.scroll.y, this.scroll.w, this.scroll.h, context);

        for (int i = 0; i < c; i++)
        {
            String label = String.valueOf(i);
            int xx = this.scroll.x + i * this.scroll.scrollItemSize - this.scroll.scroll;
            int w = this.font.getStringWidth(label);

            Gui.drawRect(xx, y, xx + 20, y + 20, this.index == i ? 0xffcc1170 : 0xffff2280);
            Gui.drawRect(xx + 19, y, xx + 20, y + 20, 0x22000000);
            this.font.drawStringWithShadow(label, xx + 10 - w / 2, y + 6, 0xffffff);
        }

        GuiDraw.unscissor(context);

        /* Display scroll bar */
        int mw = this.scroll.w;
        int scroll = this.scroll.getScrollBar(mw);

        if (scroll != 0)
        {
            int bx = this.scroll.x + (int) (this.scroll.scroll / (float) (this.scroll.scrollSize - this.scroll.w) * (mw - scroll));
            int by = y + this.scroll.h + 2;

            Gui.drawRect(bx, by, bx + scroll, by + 2, 0x88000000);
        }

        /* Overlay "shadows" for informing the user that  */
        if (this.scroll.scroll > 0 && this.scroll.scrollSize >= this.scroll.w - 40)
        {
            GuiDraw.drawHorizontalGradientRect(x, y, x + 4, y + this.scroll.h, 0x88000000, 0x0, 0);
        }

        if (this.scroll.scroll < this.scroll.scrollSize - this.scroll.w && this.scroll.scrollSize >= this.scroll.w)
        {
            GuiDraw.drawHorizontalGradientRect(x + this.scroll.w - 4, y, x + this.scroll.w, y + this.scroll.h, 0x0, 0x88000000, 0);
        }

        super.draw(context);

        String label = I18n.format("aperture.gui.panels.path_points");
        int w = this.font.getStringWidth(label);
        this.font.drawStringWithShadow(label, this.scroll.x + this.scroll.w / 2 - w / 2, this.scroll.y - 14, 0xffffff);
    }

    /**
     * Point picker interface
     */
    public static interface IPointPicker
    {
        public void pickPoint(GuiPointsModule module, int index);
    }
}