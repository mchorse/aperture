package mchorse.aperture.client.gui.utils;

import java.util.List;

import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.panels.GuiKeyframeFixturePanel;
import mchorse.aperture.client.gui.utils.GuiDopeSheet.GuiSheet;
import net.minecraft.client.Minecraft;

public class GuiFixtureKeyframesDopeSheetEditor extends GuiFixtureKeyframesEditor<GuiDopeSheet, GuiKeyframeFixturePanel>
{
    public GuiFixtureKeyframesDopeSheetEditor(Minecraft mc, GuiKeyframeFixturePanel parent)
    {
        super(mc, parent);

        this.value.setVisible(false);
        this.interpolations.flex().h(1, -30);
    }

    @Override
    protected GuiDopeSheet createElement(Minecraft mc)
    {
        return new GuiDopeSheet(mc, (frame) -> this.fillData(frame));
    }

    public void setFixture(KeyframeFixture fixture)
    {
        List<GuiSheet> sheets = this.graph.sheets;

        sheets.clear();

        for (int i = 0; i < this.parent.titles.length; i++)
        {
            KeyframeChannel channel = i == 0 ? this.parent.allChannel : fixture.channels[i - 1];

            sheets.add(new GuiSheet(this.parent.titles[i], this.parent.colors[i], channel));
        }

        this.graph.resetView();
        this.value.setVisible(false);

        this.interpolations.setVisible(false);
        this.frameButtons.setVisible(false);
    }
}