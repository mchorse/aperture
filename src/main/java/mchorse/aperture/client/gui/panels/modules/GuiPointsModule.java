package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.undo.FixturePointsChangeUndo;
import mchorse.aperture.utils.undo.IUndo;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.ScrollArea;
import mchorse.mclib.client.gui.utils.ScrollDirection;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;

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
    public Consumer<Integer> picker;

    /* GUI */
    public ScrollArea scroll = new ScrollArea(20);

    /**
     * Currently selected button (shouldn't be deselected, i.e. can't be -1)
     */
    public int index = 0;

    public GuiPointsModule(Minecraft mc, GuiCameraEditor editor, Consumer<Integer> picker)
    {
        super(mc, editor);

        this.picker = picker;

        GuiIconElement back = new GuiIconElement(mc, Icons.SHIFT_BACKWARD, (b) -> this.moveBack());
        GuiIconElement forward = new GuiIconElement(mc, Icons.SHIFT_FORWARD, (b) -> this.moveForward());
        GuiIconElement add = new GuiIconElement(mc, Icons.ADD, (b) -> this.addPoint());
        GuiIconElement remove = new GuiIconElement(mc, Icons.REMOVE, (b) -> this.removePoint());

        back.flex().relative(this).x(-40);
        remove.flex().relative(this).x(-20);
        add.flex().relative(this).x(1F);
        forward.flex().relative(this).x(1F, 20);

        this.add(back, add, remove, forward);
        this.scroll.direction = ScrollDirection.HORIZONTAL;
    }

    private IUndo<CameraProfile> undo(GuiCameraEditor editor, int index, int nextIndex, List<Position> positions)
    {
        CameraProfile profile = editor.getProfile();
        AbstractFixture fixture = editor.getFixture();
        int fixtureIndex = profile.fixtures.indexOf(fixture);

        return new FixturePointsChangeUndo(fixtureIndex, this.path.points.getPath(), index, nextIndex, this.path.points.getValue(), positions).view(this.editor.timeline).noMerging();
    }

    public void setIndex(int index)
    {
        this.index = index;
        this.scroll.scrollIntoView(index * this.scroll.scrollItemSize);
    }

    public void moveBack()
    {
        if (this.index == 0)
        {
            return;
        }

        List<Position> positions = (List<Position>) this.path.points.getValue();

        positions.add(this.index, positions.remove(this.index - 1));

        int nextIndex = this.index - 1;

        this.editor.postUndo(this.undo(this.editor, this.index, nextIndex, positions));
        this.index = nextIndex;
    }

    public void moveForward()
    {
        if (this.index >= this.path.size() - 1)
        {
            return;
        }

        List<Position> positions = (List<Position>) this.path.points.getValue();

        positions.add(this.index, positions.remove(this.index + 1));

        int nextIndex = this.index - 1;

        this.editor.postUndo(this.undo(this.editor, this.index, nextIndex, positions));
        this.index = nextIndex;
    }

    public void addPoint()
    {
        List<Position> positions = (List<Position>) this.path.points.getValue();

        if (this.index + 1 >= this.path.size())
        {
            positions.add(this.editor.getPosition());

            int nextIndex = MathHelper.clamp(this.index + 1, 0, positions.size() - 1);

            this.editor.postUndo(this.undo(this.editor, this.index, nextIndex, positions));
            this.index = nextIndex;
        }
        else
        {
            positions.add(this.index + 1, this.editor.getPosition());

            int nextIndex = this.index + 1;

            this.editor.postUndo(this.undo(this.editor, this.index, nextIndex, positions));
            this.index = nextIndex;
        }

        this.scroll.setSize(this.path.size());
        this.scroll.scrollTo(this.index * this.scroll.scrollItemSize);

        if (this.picker != null)
        {
            this.picker.accept(this.index);
        }
    }

    public void removePoint()
    {
        if (this.path.points.size() == 1 && this.index >= 0)
        {
            return;
        }

        List<Position> positions = (List<Position>) this.path.points.getValue();

        positions.remove(this.index);

        int nextIndex = this.index > 0 ? this.index - 1 : this.index;

        this.editor.postUndo(this.undo(this.editor, this.index, nextIndex, positions));

        this.index = nextIndex;
        this.scroll.setSize(this.path.size());
        this.scroll.scrollTo(this.index * this.scroll.scrollItemSize);

        if (this.picker != null)
        {
            this.picker.accept(this.index);
        }
    }

    /**
     * Setup the path fixture and also fill or reset this module's fields based
     * on the path fixture.
     */
    public void fill(PathFixture path)
    {
        this.path = path;
        this.index = 0;
        this.scroll.setSize(path.size());
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
                int size = this.path.size();

                if (index >= 0 && index < size)
                {
                    /* Pick a point */
                    this.index = index;

                    if (this.picker != null)
                    {
                        this.picker.accept(index);
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
     * selecting a point in the path or shifting the playback timeline to the
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
        int c = this.path.size();

        /* Draw background and buttons */
        Gui.drawRect(x, y, x + this.scroll.w, y + this.scroll.h, ColorUtils.HALF_BLACK);
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

            Gui.drawRect(bx, by, bx + scroll, by + 2, ColorUtils.HALF_BLACK);
        }

        /* Overlay "shadows" for informing the user that  */
        if (this.scroll.scroll > 0 && this.scroll.scrollSize >= this.scroll.w - 40)
        {
            GuiDraw.drawHorizontalGradientRect(x, y, x + 4, y + this.scroll.h, ColorUtils.HALF_BLACK, 0, 0);
        }

        if (this.scroll.scroll < this.scroll.scrollSize - this.scroll.w && this.scroll.scrollSize >= this.scroll.w)
        {
            GuiDraw.drawHorizontalGradientRect(x + this.scroll.w - 4, y, x + this.scroll.w, y + this.scroll.h, 0, ColorUtils.HALF_BLACK, 0);
        }

        super.draw(context);

        String label = I18n.format("aperture.gui.panels.path_points");
        int w = this.font.getStringWidth(label);
        this.font.drawStringWithShadow(label, this.scroll.x + this.scroll.w / 2 - w / 2, this.scroll.y - 14, 0xffffff);
    }
}