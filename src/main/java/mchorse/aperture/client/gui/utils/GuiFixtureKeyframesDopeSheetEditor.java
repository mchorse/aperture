package mchorse.aperture.client.gui.utils;

import mchorse.aperture.client.gui.panels.keyframe.AllKeyframe;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiDopeSheet.GuiSheet;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.client.gui.panels.GuiKeyframeFixturePanel;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;

import java.util.List;

public class GuiFixtureKeyframesDopeSheetEditor extends GuiFixtureKeyframesEditor<GuiDopeSheet, GuiKeyframeFixturePanel>
{
    public GuiFixtureKeyframesDopeSheetEditor(Minecraft mc, GuiKeyframeFixturePanel parent)
    {
        super(mc, parent);

        this.graph.panel = parent;
        this.interpolations.flex().h(1, -30);
    }

    @Override
    protected GuiDopeSheet createElement(Minecraft mc)
    {
        return new GuiDopeSheet(mc, this::fillData);
    }

    @Override
    public void fillData(Keyframe frame)
    {
        super.fillData(frame);

        this.value.setVisible(!(frame instanceof AllKeyframe));
    }

    public void setFixture(KeyframeFixture fixture)
    {
        List<GuiSheet> sheets = this.graph.sheets;

        sheets.clear();

        for (int i = 0; i < this.panel.titles.length; i++)
        {
            KeyframeChannel channel = i == 0 ? this.panel.allChannel : fixture.channels[i - 1];

            sheets.add(new GuiSheet(this.panel.titles[i], this.panel.colors[i], channel));
        }

        this.graph.resetView();
        this.value.setVisible(false);

        this.interpolations.setVisible(false);
        this.frameButtons.setVisible(false);
    }
}