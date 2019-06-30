package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import net.minecraft.client.Minecraft;

public abstract class GuiKeyframeEditor extends GuiElement
{
    public GuiAbstractFixturePanel<? extends AbstractFixture> parent;

    public GuiKeyframeEditor(Minecraft mc)
    {
        super(mc);
    }

    public abstract Keyframe getCurrent();

    public abstract void setChannel(KeyframeChannel channel);

    public abstract void setColor(int color);

    public abstract void setDuration(long duration);

    public abstract void setSliding();

    public abstract void selectByDuration(long duration);

    public void setParent(GuiAbstractFixturePanel<? extends AbstractFixture> parent)
    {
        this.parent = parent;
    }

    public abstract void doubleClick(int mouseX, int mouseY);

    public abstract void addCurrent(long tick, float value);

    public abstract void removeCurrent();
}