package mchorse.aperture.client.gui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.utils.Scale;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import net.minecraft.client.Minecraft;

public class GuiDopeSheet extends GuiElement
{
    public Consumer<Keyframe> callback;
    public List<GuiSheet> sheets = new ArrayList<GuiSheet>();
    public Scale scale = new Scale(false);

    public GuiDopeSheet(Minecraft mc, Consumer<Keyframe> callback)
    {
        super(mc);

        this.callback = callback;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }

    public static class GuiSheet
    {
        public String title = "";
        public KeyframeChannel channel;

        public GuiSheet(String title, KeyframeChannel channel)
        {
            this.title = title;
            this.channel = channel;
        }
    }
}