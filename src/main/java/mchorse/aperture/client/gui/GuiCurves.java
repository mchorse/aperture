package mchorse.aperture.client.gui;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.values.ValueCurves;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesGraphEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;

public class GuiCurves extends GuiElement
{
    public GuiCameraEditor editor;
    public GuiCameraEditorKeyframesGraphEditor keyframes;

    public GuiCurves(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        this.keyframes = new GuiCameraEditorKeyframesGraphEditor(mc, editor);
        this.keyframes.graph.global = true;
        this.keyframes.flex().relative(this).wh(1F, 1F);

        this.add(this.keyframes);
    }

    public void updateKeyframeEditor()
    {
        this.keyframes.updateConverter();
    }

    public void updateDuration()
    {
        this.keyframes.graph.duration = (int) this.editor.getProfile().getDuration();
    }

    public void update()
    {
        String main = "brightness";

        CameraProfile profile = this.editor.getProfile();
        ValueCurves channels = profile.curves;
        KeyframeChannel channel = channels.get(main);

        if (channel == null)
        {
            channel = new KeyframeChannel();
            channels.put(main, channel);
        }

        if (this.keyframes.graph.sheet.channel == channel)
        {
            return;
        }

        this.updateDuration();
        this.keyframes.setChannel(profile.getProperty(profile.curves.id + "." + main), 0xff1493);
    }

    @Override
    public void draw(GuiContext context)
    {


        super.draw(context);
    }
}