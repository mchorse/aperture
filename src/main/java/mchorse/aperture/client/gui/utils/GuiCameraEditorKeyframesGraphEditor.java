package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.values.ValueKeyframeChannel;
import mchorse.aperture.camera.values.ValueProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.config.values.IConfigValue;
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
        return new GuiGraphView(mc, this, this::fillData);
    }

    public void setChannel(IConfigValue value, int color)
    {
        ValueKeyframeChannel keyframe = this.get(value);

        if (keyframe == null)
        {
            throw new IllegalStateException("Given value doesn't have a keyframe channel! " + value.getClass().getSimpleName());
        }

        this.graph.clearSelection();
        this.graph.setChannel(keyframe.get(), color);
        this.interpolations.setVisible(false);
        this.frameButtons.setVisible(false);

        this.valueChannels.clear();
        this.valueChannels.add(value);
    }
}