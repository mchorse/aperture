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
        this.interpolations.flex().h(1, -30);
    }

    @Override
    protected GuiDopeSheet createElement(Minecraft mc)
    {
        return new GuiDopeSheet(mc, this::fillData);
    }

    public void setFixture(KeyframeFixture fixture)
    {
        List<GuiSheet> sheets = this.graph.sheets;

        sheets.clear();

        if (this.editor.panel.delegate == null)
        {
            return;
        }

        GuiKeyframeFixturePanel panel = (GuiKeyframeFixturePanel) this.editor.panel.delegate;

        for (int i = 0; i < fixture.channels.length; i++)
        {
            sheets.add(new GuiSheet(panel.titles[i + 1], panel.colors[i], fixture.channels[i]));
        }

        this.graph.resetView();
        this.value.setVisible(false);

        this.interpolations.setVisible(false);
        this.frameButtons.setVisible(false);
    }
}