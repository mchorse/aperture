package mchorse.aperture.client.gui.config;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import net.minecraft.client.Minecraft;

public abstract class GuiAbstractConfigOptions extends GuiElement
{
    public GuiCameraEditor editor;

    public GuiAbstractConfigOptions(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        this.createChildren();
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void update();

    public abstract boolean isActive();
}