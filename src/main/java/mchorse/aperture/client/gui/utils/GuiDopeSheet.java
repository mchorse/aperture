package mchorse.aperture.client.gui.utils;

import mchorse.aperture.client.gui.dashboard.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.utils.keyframes.Keyframe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.function.Consumer;

public class GuiDopeSheet extends mchorse.mclib.client.gui.framework.elements.keyframes.GuiDopeSheet
{
    public GuiCameraEditor editor;

    public GuiDopeSheet(Minecraft mc, Consumer<Keyframe> callback)
    {
        super(mc, callback);
    }

    public long getFixtureOffset()
    {
        if (this.editor == null || this.editor.panel.delegate == null)
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

        return (int) (this.editor.dashboard.timeline.value - this.getFixtureOffset());
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

            this.editor.dashboard.timeline.setValueFromScrub((int) (x + offset));
        }
    }

    @Override
    protected void drawCursor(GuiContext context)
    {
        if (this.editor != null)
        {
            int cx = this.getOffset();

            cx = this.toGraphX(cx);

            Gui.drawRect(cx - 1, this.area.y, cx + 1, this.area.ey(), 0xff57f52a);
        }
    }
}