package mchorse.aperture.client.gui.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeInterpolation;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

/**
 * Interpolations list
 */
public class GuiKeyframeInterpolationsList extends GuiListElement<KeyframeInterpolation>
{
    public GuiKeyframeInterpolationsList(Minecraft mc, Consumer<List<KeyframeInterpolation>> callback)
    {
        super(mc, callback);

        this.scroll.scrollItemSize = 16;

        for (KeyframeInterpolation interp : KeyframeInterpolation.values())
        {
            this.add(interp);
        }

        this.sort();
        this.background();
    }

    @Override
    protected boolean sortElements()
    {
        Collections.sort(this.list, (o1, o2) -> o1.key.compareTo(o2.key));

        return true;
    }

    @Override
    protected String elementToString(KeyframeInterpolation element, int i, int x, int y, boolean hover, boolean selected)
    {
        return I18n.format("aperture.gui.panels.interps." + element.key);
    }
}