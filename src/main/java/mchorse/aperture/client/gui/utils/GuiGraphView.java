package mchorse.aperture.client.gui.utils;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.utils.keyframes.Keyframe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.function.Consumer;

public class GuiGraphView extends mchorse.mclib.client.gui.framework.elements.keyframes.GuiGraphView
{
    public GuiCameraEditor editor;
    public boolean global;

    public GuiGraphView(Minecraft mc, Consumer<Keyframe> callback)
    {
        super(mc, callback);
    }

    public long getFixtureOffset()
    {
        if (this.editor == null || this.editor.panel.delegate == null || this.global)
        {
            return 0;
        }

        return this.editor.getProfile().calculateOffset(this.editor.panel.delegate.fixture);
    }

    public int getOffset()
    {
        if (this.editor == null)
        {
            return 0;
        }

        return (int) (this.editor.timeline.value - this.getFixtureOffset());
    }

    @Override
    protected void updateMoved()
    {
        if (this.editor != null)
        {
            this.editor.updateProfile();
        }
    }

    @Override
    protected void moveNoKeyframe(GuiContext context, Keyframe frame, double x, double y)
    {
        if (this.editor != null)
        {
            long offset = this.getFixtureOffset();

            this.editor.timeline.setValueFromScrub((int) (x + offset));
        }
    }

    @Override
    protected void drawCursor(GuiContext context)
    {
        if (this.editor != null)
        {
            int cx = this.getOffset();
            int cy = this.toGraphY(this.sheet.channel.interpolate(cx));

            cx = this.toGraphX(cx);

            if (cy < this.area.ey() && cx >= this.area.x && cx <= this.area.ex())
            {
                Gui.drawRect(cx - 1, cy, cx + 1, this.area.ey(), 0xff57f52a);
            }
        }
    }
}