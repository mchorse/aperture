package mchorse.aperture.utils;

import net.minecraft.util.math.MathHelper;

/**
 * Scrollable area
 * 
 * This class is responsible for storing information for scrollable one 
 * directional objects. 
 */
public class ScrollArea extends Rect
{
    /**
     * Size of an element/item in the scroll area
     */
    public int scrollItemSize;

    /**
     * Size of the scrolling area 
     */
    public int scrollSize;

    /**
     * Scroll position 
     */
    public int scroll;

    public ScrollArea(int itemSize)
    {
        this.scrollItemSize = itemSize;
    }

    public void setSize(int items)
    {
        this.scrollSize = items * this.scrollItemSize;
    }

    public void scrollBy(int x)
    {
        this.scroll += x;
        this.clamp();
    }

    public void scrollTo(int x)
    {
        this.scroll = x;
        this.clamp();
    }

    public void clamp()
    {
        if (this.scrollSize <= this.h)
        {
            this.scroll = 0;
        }
        else
        {
            this.scroll = MathHelper.clamp(this.scroll, 0, this.scrollSize - this.h);
        }
    }

    public int getIndex(int x, int y)
    {
        if (!this.isInside(x, y))
        {
            return -1;
        }

        y -= this.y;
        y += this.scroll;

        int index = y / this.scrollItemSize;

        return index > this.scrollSize / this.scrollItemSize ? -1 : index;
    }
}