package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.utils.keyframes.Keyframe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.function.Consumer;

public class GuiGraphView extends mchorse.mclib.client.gui.framework.elements.keyframes.GuiGraphView
{
    public GuiAbstractFixturePanel<? extends AbstractFixture> panel;

    public GuiGraphView(Minecraft mc, Consumer<Keyframe> callback)
    {
        super(mc, callback);
    }

    public int getOffset()
    {
        if (this.panel == null)
        {
            return 0;
        }

        return (int) (this.panel.editor.timeline.value - this.panel.editor.getProfile().calculateOffset(this.panel.fixture));
    }

    @Override
    protected void updateMoved()
    {
        if (this.panel != null)
        {
            this.panel.editor.updateProfile();
        }
    }

    @Override
    protected void moveNoKeyframe(GuiContext context, Keyframe frame, double x, double y)
    {
        if (this.panel != null)
        {
            long offset = this.panel.editor.getProfile().calculateOffset(this.panel.fixture);

            this.panel.editor.timeline.setValueFromScrub((int) (x + offset));
        }
    }

    @Override
    protected void drawCursor(GuiContext context)
    {
        if (this.panel != null)
        {
            int cx = this.getOffset();
            int cy = this.toGraphY(this.channel.interpolate(cx));

            cx = this.toGraphX(cx);

            if (cy < this.area.ey() && cx >= this.area.x && cx <= this.area.ex())
            {
                Gui.drawRect(cx - 1, cy, cx + 1, this.area.ey(), 0xff57f52a);
            }
        }
    }
}