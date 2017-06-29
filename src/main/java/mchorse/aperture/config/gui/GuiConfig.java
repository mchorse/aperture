package mchorse.aperture.config.gui;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.Aperture;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Config GUI
 *
 * This config GUI is responsible for managing Aperture's config. Most of
 * the code that implements config features is located in the parent of the
 * class.
 */
@SideOnly(Side.CLIENT)
public class GuiConfig extends net.minecraftforge.fml.client.config.GuiConfig
{
    public GuiConfig(GuiScreen parent)
    {
        super(parent, getConfigElements(), Aperture.MODID, false, false, Aperture.MODNAME);
    }

    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> elements = new ArrayList<IConfigElement>();

        for (String name : Aperture.proxy.forge.getCategoryNames())
        {
            ConfigCategory category = Aperture.proxy.forge.getCategory(name);

            category.setLanguageKey("aperture.config." + name + ".title");
            category.setComment(I18n.format("aperture.config.category." + name + ".tooltip"));

            if (name.indexOf(".") == -1 && !name.equals("camera"))
            {
                elements.add(new TranslatedConfigElement(category));
            }
        }

        for (Property prop : Aperture.proxy.forge.getCategory("camera").getOrderedValues())
        {
            elements.add(new TranslatedConfigElement(prop));
        }

        return elements;
    }
}