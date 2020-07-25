package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.keyframe.AllKeyframe;
import mchorse.aperture.client.gui.panels.keyframe.AllKeyframeChannel;
import mchorse.aperture.client.gui.panels.keyframe.KeyframeCell;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
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

        return (int) (this.editor.timeline.value - this.getFixtureOffset());
    }

    @Override
    protected void addedDoubleClick(Keyframe frame, long tick, int mouseX, int mouseY)
    {
        if (frame instanceof AllKeyframe)
        {
            AllKeyframeChannel all = (AllKeyframeChannel) this.current.channel;
            AllKeyframe key = (AllKeyframe) frame;
            Position pos = new Position(this.editor.getCamera());

            double value = 0;

            for (KeyframeChannel channel : all.fixture.channels)
            {
                if (channel == all.fixture.x) value = pos.point.x;
                if (channel == all.fixture.y) value = pos.point.y;
                if (channel == all.fixture.z) value = pos.point.z;
                if (channel == all.fixture.yaw) value = pos.angle.yaw;
                if (channel == all.fixture.pitch) value = pos.angle.pitch;
                if (channel == all.fixture.roll) value = pos.angle.roll;
                if (channel == all.fixture.fov) value = pos.angle.fov;

                int index = channel.insert(tick, value);

                key.keyframes.add(new KeyframeCell(channel.getKeyframes().get(index), channel));
            }
        }
    }

    @Override
    protected void finishSorting()
    {
        for (GuiSheet sheet : this.sheets)
        {
            if (sheet.channel instanceof AllKeyframeChannel)
            {
                AllKeyframeChannel channel = (AllKeyframeChannel) sheet.channel;

                channel.setFixture(channel.fixture);
            }
        }
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

            cx = this.toGraph(cx);

            Gui.drawRect(cx - 1, this.area.y, cx + 1, this.area.ey(), 0xff57f52a);
        }
    }
}