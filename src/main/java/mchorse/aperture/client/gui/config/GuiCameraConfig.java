package mchorse.aperture.client.gui.config;

import mchorse.aperture.client.gui.dashboard.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import net.minecraft.client.Minecraft;

public class GuiCameraConfig extends GuiScrollElement
{
    public GuiCameraEditor editor;

    public GuiCameraConfig(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;

        this.add(editor.cameraOptions);
        this.flex().column(0).vertical().stretch().scroll();
    }

    @Override
    public void draw(GuiContext context)
    {
        this.area.draw(0xaa000000);

        super.draw(context);
    }
}