package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiKeyframeFixturePanel;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiSheet;
import net.minecraft.client.Minecraft;

import java.util.List;

public class GuiCameraEditorKeyframesDopeSheetEditor extends GuiCameraEditorKeyframesEditor<GuiDopeSheet>
{
    public GuiCameraEditorKeyframesDopeSheetEditor(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.graph.editor = editor;
    }

    @Override
    protected GuiDopeSheet createElement(Minecraft mc)
    {
        return new GuiDopeSheet(mc, this, this::fillData);
    }

    public void setFixture(KeyframeFixture fixture)
    {
        List<GuiSheet> sheets = this.graph.sheets;

        sheets.clear();
        this.graph.clearSelection();

        if (this.editor.panel.delegate == null)
        {
            return;
        }

        GuiKeyframeFixturePanel panel = (GuiKeyframeFixturePanel) this.editor.panel.delegate;

        this.valueChannels.clear();

        for (int i = 0; i < fixture.channels.length; i++)
        {
            this.valueChannels.add(fixture.channels[i]);
            sheets.add(new GuiSheet(String.valueOf(i), panel.titles[i + 1], panel.colors[i], fixture.channels[i].get()));
        }

        this.graph.resetView();

        this.interpolations.setVisible(false);
        this.frameButtons.setVisible(false);
    }
}