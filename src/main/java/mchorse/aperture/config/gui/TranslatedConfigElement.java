package mchorse.aperture.config.gui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;

/**
 * Translated config element
 * 
 * I hate private fields which are part of the API.
 */
public class TranslatedConfigElement extends ConfigElement
{
    public TranslatedConfigElement(ConfigCategory category)
    {
        super(category);
    }

    public TranslatedConfigElement(Property prop)
    {
        super(prop);
    }

    @Override
    public String getComment()
    {
        return I18n.format(this.getLanguageKey().replace("aperture.config", "aperture.config.comments"));
    }

    @Override
    public List<IConfigElement> getChildElements()
    {
        if (!this.isProperty())
        {
            List<IConfigElement> elements = new ArrayList<IConfigElement>();

            /* OMG, why forge? Why??? */
            Field[] fields = ConfigElement.class.getDeclaredFields();
            Field categoryField = null;

            for (Field field : fields)
            {
                if (field.getType().equals(ConfigCategory.class))
                {
                    categoryField = field;
                    categoryField.setAccessible(true);

                    break;
                }
            }

            if (categoryField == null)
            {
                return null;
            }

            try
            {
                ConfigCategory category = (ConfigCategory) categoryField.get(this);

                Iterator<ConfigCategory> ccI = category.getChildren().iterator();
                Iterator<Property> pI = category.getOrderedValues().iterator();

                while (ccI.hasNext())
                {
                    ConfigElement temp = new TranslatedConfigElement(ccI.next());

                    if (temp.showInGui())
                    {
                        elements.add(temp);
                    }
                }

                while (pI.hasNext())
                {
                    ConfigElement prop = new TranslatedConfigElement(pI.next());

                    if (prop.showInGui())
                    {
                        elements.add(prop);
                    }
                }

                return elements;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
}