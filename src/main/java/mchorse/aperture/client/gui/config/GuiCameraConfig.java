package mchorse.aperture.client.gui.config;

import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.resizers.layout.ColumnResizer;
import org.lwjgl.opengl.GL11;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.events.CameraEditorEvent;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.ScrollArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

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

        ColumnResizer.apply(this, 0).vertical().stretch().scroll();
    }

    @Override
    public void draw(GuiContext context)
    {
        this.area.draw(0xaa000000);

        super.draw(context);
    }
}