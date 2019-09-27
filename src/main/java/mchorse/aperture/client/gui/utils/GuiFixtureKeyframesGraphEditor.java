package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import net.minecraft.client.Minecraft;

/**
 * Graph editor GUI designed specifically for keyframe fixture panel
 */
public class GuiFixtureKeyframesGraphEditor<T extends GuiAbstractFixturePanel<? extends AbstractFixture>> extends GuiFixtureKeyframesEditor<GuiGraphView, T>
{
    public GuiFixtureKeyframesGraphEditor(Minecraft mc, T parent)
    {
        super(mc, parent);
    }

    @Override
    protected GuiGraphView createElement(Minecraft mc)
    {
        return new GuiGraphView(mc, (frame) -> this.fillData(frame));
    }

    public void setChannel(KeyframeChannel channel)
    {
        this.graph.setChannel(channel);
        this.interpolations.setVisible(false);
        this.frameButtons.setVisible(false);
        this.graph.selected = -1;
    }
}