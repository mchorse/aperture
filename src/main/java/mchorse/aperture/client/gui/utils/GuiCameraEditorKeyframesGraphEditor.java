package mchorse.aperture.client.gui.utils;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;

/**
 * Graph editor GUI designed specifically for keyframe fixture panel
 */
public class GuiCameraEditorKeyframesGraphEditor extends GuiCameraEditorKeyframesEditor<GuiGraphView>
{
    public GuiCameraEditorKeyframesGraphEditor(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.graph.editor = editor;
    }

    @Override
    protected GuiGraphView createElement(Minecraft mc)
    {
        return new GuiGraphView(mc, this::fillData);
    }

    public void setChannel(KeyframeChannel channel)
    {
        this.graph.clearSelection();
        this.graph.setChannel(channel);
        this.interpolations.setVisible(false);
        this.frameButtons.setVisible(false);
    }
}