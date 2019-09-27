package mchorse.aperture.client.gui.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;

import mchorse.aperture.camera.fixtures.PathFixture.InterpolationType;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

public class GuiInterpolationTypeList extends GuiListElement<InterpolationType>
{
    public GuiInterpolationTypeList(Minecraft mc, Consumer<InterpolationType> callback)
    {
        super(mc, callback);

        this.scroll.scrollItemSize = 16;

        for (InterpolationType interp : InterpolationType.values())
        {
            this.add(interp);
        }

        this.sort();
    }

    @Override
    public void sort()
    {
        Collections.sort(this.list, new Comparator<InterpolationType>()
        {
            @Override
            public int compare(InterpolationType o1, InterpolationType o2)
            {
                return o1.name.compareTo(o2.name);
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
    public void drawElement(InterpolationType element, int i, int x, int y, boolean hover)
    {
        if (this.current == i)
        {
            Gui.drawRect(x, y, x + this.scroll.w, y + this.scroll.scrollItemSize, 0x880088ff);
        }

        String label = I18n.format("aperture.gui.panels.interps." + element.name);

        this.font.drawStringWithShadow(label, x + 4, y + 4, hover ? 16777120 : 0xffffff);
    }
}