package mchorse.aperture.utils.mclib;

import java.util.Arrays;
import java.util.List;

import mchorse.aperture.Aperture;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.ValueGUI;
import net.minecraft.client.Minecraft;
import net.optifine.shaders.Shaders;

public class ValueShaderOption extends ValueGUI
{
    public ValueShaderOption(String id)
    {
        super(id);
    }

    @Override
    public List<GuiElement> getFields(Minecraft mc, GuiConfigPanel config)
    {
        GuiToggleElement toggle = new GuiToggleElement(mc, Aperture.optifineShaderOptionCurve, element -> 
        {
            if (Shaders.shaderPackLoaded)
            {
                Shaders.uninit();
                Shaders.loadShaderPack();
            }
        });

        toggle.flex().reset();

        return Arrays.asList(toggle);
    }
}
