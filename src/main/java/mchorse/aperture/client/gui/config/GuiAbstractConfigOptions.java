package mchorse.aperture.client.gui.config;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import net.minecraft.client.Minecraft;

public abstract class GuiAbstractConfigOptions extends GuiElement
{
    public GuiCameraEditor editor;

    public GuiAbstractConfigOptions(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;

        this.add(Elements.label(this.getTitle()).background(0x88000000));
        this.flex().column(5).vertical().stretch().height(20).padding(10);
    }

    public abstract void update();

    public abstract String getTitle();
}