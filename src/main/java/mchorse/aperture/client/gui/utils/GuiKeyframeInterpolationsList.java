package mchorse.aperture.client.gui.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;

import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeInterpolation;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

/**
 * Interpolations list
 */
public class GuiKeyframeInterpolationsList extends GuiListElement<KeyframeInterpolation>
{
    public GuiKeyframeInterpolationsList(Minecraft mc, Consumer<KeyframeInterpolation> callback)
    {
        super(mc, callback);

        this.scroll.scrollItemSize = 16;

        for (KeyframeInterpolation interp : KeyframeInterpolation.values())
        {
            this.add(interp);
        }

        this.sort();
    }

    @Override
    public void sort()
    {
        Collections.sort(this.list, new Comparator<KeyframeInterpolation>()
        {
            @Override
            public int compare(KeyframeInterpolation o1, KeyframeInterpolation o2)
            {
                return o1.key.compareTo(o2.key);
            }
        });

        this.update();
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        this.scroll.draw(0x88000000);

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawElement(KeyframeInterpolation element, int i, int x, int y, boolean hover)
    {
        if (this.current == i)
        {
            Gui.drawRect(x, y, x + this.scroll.w, y + this.scroll.scrollItemSize, 0x880088ff);
        }

        String label = I18n.format("aperture.gui.panels.interps." + element.key);

        this.font.drawStringWithShadow(label, x + 4, y + 4, hover ? 16777120 : 0xffffff);
    }
}