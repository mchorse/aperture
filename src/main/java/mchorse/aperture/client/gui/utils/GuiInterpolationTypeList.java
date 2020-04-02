package mchorse.aperture.client.gui.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import mchorse.aperture.camera.fixtures.PathFixture.InterpolationType;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

public class GuiInterpolationTypeList extends GuiListElement<InterpolationType>
{
    public GuiInterpolationTypeList(Minecraft mc, Consumer<List<InterpolationType>> callback)
    {
        super(mc, callback);

        this.scroll.scrollItemSize = 16;

        for (InterpolationType interp : InterpolationType.values())
        {
            this.add(interp);
        }

        this.sort();
        this.background();
    }

    @Override
    protected boolean sortElements()
    {
        Collections.sort(this.list, (o1, o2) -> o1.name.compareTo(o2.name));

        return true;
    }

    @Override
    protected String elementToString(InterpolationType element, int i, int x, int y, boolean hover, boolean selected)
    {
        return I18n.format("aperture.gui.panels.interps." + element.name);
    }
}