package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeInterpolation;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import net.minecraft.client.Minecraft;

/**
 * Special subclass of graph editor for fixture editor panels to allow 
 * dirtying the camera profile.
 */
public abstract class GuiFixtureKeyframesEditor<E extends GuiKeyframeElement, T extends GuiAbstractFixturePanel<? extends AbstractFixture>> extends GuiKeyframesEditor<E>
{
    protected T parent;

    public GuiFixtureKeyframesEditor(Minecraft mc, T parent)
    {
        super(mc);

        this.parent = parent;
        this.graph.setParent(parent);

        this.interp.keys().keybinds.get(0).active(parent.editor::isFlightMode);
        this.easing.keys().keybinds.get(0).active(parent.editor::isFlightMode);
    }

    @Override
    protected void toggleInterpolation()
    {
        super.toggleInterpolation();
        this.parent.editor.updateProfile();
    }

    @Override
    protected void doubleClick(int mouseX, int mouseY)
    {
        super.doubleClick(mouseX, mouseY);
        this.parent.editor.updateProfile();
    }

    @Override
    public void setTick(long value)
    {
        super.setTick(value);
        this.parent.editor.updateProfile();
    }

    @Override
    public void setValue(double value)
    {
        super.setValue(value);
        this.parent.editor.updateProfile();
    }

    @Override
    public void changeEasing()
    {
        super.changeEasing();
        this.parent.editor.updateProfile();
    }

    @Override
    public void pickInterpolation(KeyframeInterpolation interp)
    {
        super.pickInterpolation(interp);
        this.parent.editor.updateProfile();
    }
}