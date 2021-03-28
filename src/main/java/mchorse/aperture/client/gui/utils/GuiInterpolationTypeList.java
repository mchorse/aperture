package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.data.InterpolationType;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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

        this.background().cancelScrollEdge().sort();
    }

    @Override
    protected boolean sortElements()
    {
        Collections.sort(this.list, Comparator.comparing(o -> o.name));

        return true;
    }

    @Override
    protected String elementToString(InterpolationType element)
    {
        return I18n.format(element.getKey());
    }
}