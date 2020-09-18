package mchorse.aperture.client.gui.config;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.events.CameraEditorEvent;
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

        CameraEditorEvent.Options event = new CameraEditorEvent.Options(editor);
        event.options.add(editor.cameraOptions);

        ClientProxy.EVENT_BUS.post(event);

        for (GuiAbstractConfigOptions option : event.options)
        {
            this.add(option);
        }

        this.flex().column(0).vertical().stretch().scroll();
    }

    @Override
    public void draw(GuiContext context)
    {
        this.area.draw(0xaa000000);

        super.draw(context);
    }
}