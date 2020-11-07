package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.client.gui.dashboard.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import net.minecraft.client.Minecraft;

public abstract class GuiAbstractModule extends GuiElement
{
    protected GuiCameraEditor editor;

    public GuiAbstractModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
    }
}