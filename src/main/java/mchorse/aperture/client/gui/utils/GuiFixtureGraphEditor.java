package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Interpolation;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import net.minecraft.client.Minecraft;

/**
 * Special subclass of graph editor for fixture editor panels to allow 
 * dirtying the camera profile.
 */
public class GuiFixtureGraphEditor<T extends GuiAbstractFixturePanel<? extends AbstractFixture>> extends GuiGraphEditor
{
    protected T parent;

    public GuiFixtureGraphEditor(Minecraft mc, T parent)
    {
        super(mc);

        this.parent = parent;
        this.graph.parent = parent;
    }

    @Override
    public void addKeyframe()
    {
        super.addKeyframe();
        this.parent.editor.updateProfile();
    }

    @Override
    public void removeKeyframe()
    {
        super.removeKeyframe();
        this.parent.editor.updateProfile();
    }

    @Override
    public void setTick(long value)
    {
        super.setTick(value);
        this.parent.editor.updateProfile();
    }

    @Override
    public void setValue(float value)
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
    public void pickInterpolation(Interpolation interp)
    {
        super.pickInterpolation(interp);
        this.parent.editor.updateProfile();
    }
}