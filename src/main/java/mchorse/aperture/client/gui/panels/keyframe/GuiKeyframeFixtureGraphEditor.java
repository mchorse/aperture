package mchorse.aperture.client.gui.panels.keyframe;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.panels.GuiKeyframeFixturePanel;
import mchorse.aperture.client.gui.utils.GuiFixtureGraphEditor;
import mchorse.aperture.client.gui.utils.GuiGraphElement;
import net.minecraft.client.Minecraft;

/**
 * Graph editor GUI designed specifically for keyframe fixture panel
 */
public class GuiKeyframeFixtureGraphEditor extends GuiFixtureGraphEditor<GuiGraphElement, GuiKeyframeFixturePanel>
{
    public GuiKeyframeFixtureGraphEditor(Minecraft mc, GuiKeyframeFixturePanel parent)
    {
        super(mc, parent);
    }

    @Override
    protected GuiGraphElement createElement(Minecraft mc)
    {
        return new GuiGraphElement(mc, (frame) -> this.fillData(frame));
    }

    @Override
    protected void doubleClick(int mouseX, int mouseY)
    {
        super.doubleClick(mouseX, mouseY);

        if (this.graph.channel == this.parent.allChannel)
        {
            Position pos = new Position(Minecraft.getMinecraft().thePlayer);
            /* Hey now, you're an */
            AllKeyframe allStar = (AllKeyframe) this.graph.getCurrent();
            /* Get your game on, go play */
            float value = 0;

            if (!allStar.keyframes.isEmpty())
            {
                return;
            }

            /* Hey now, you're a rock star */
            for (KeyframeChannel channel : this.parent.fixture.channels)
            {
                /* Get the show on, get paid */
                if (channel == this.parent.fixture.x) value = (float) pos.point.x;
                if (channel == this.parent.fixture.y) value = (float) pos.point.y;
                if (channel == this.parent.fixture.z) value = (float) pos.point.z;
                if (channel == this.parent.fixture.yaw) value = pos.angle.yaw;
                if (channel == this.parent.fixture.pitch) value = pos.angle.pitch;
                if (channel == this.parent.fixture.roll) value = pos.angle.roll;
                if (channel == this.parent.fixture.fov) value = pos.angle.fov;

                /* And all that glitters is gold */
                int index = channel.insert(allStar.tick, value);

                /* Only shooting stars break the mold */
                allStar.keyframes.add(new KeyframeCell(channel.getKeyframes().get(index), channel));
            }
        }
    }
}