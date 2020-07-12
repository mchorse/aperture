package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiKeyframeElement;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiKeyframesEditor;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.keyframes.KeyframeInterpolation;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/**
 * Special subclass of graph editor for fixture editor panels to allow 
 * dirtying the camera profile.
 */
public abstract class GuiFixtureKeyframesEditor<E extends GuiKeyframeElement, T extends GuiAbstractFixturePanel<? extends AbstractFixture>> extends GuiKeyframesEditor<E>
{
    protected T panel;

    public GuiFixtureKeyframesEditor(Minecraft mc, T panel)
    {
        super(mc);

        this.panel = panel;

        this.interp.keys().register(IKey.lang("aperture.gui.panels.keys.graph_interp"), Keyboard.KEY_LBRACKET, this::toggleInterpolation).held(Keyboard.KEY_LCONTROL).category(GuiAbstractFixturePanel.CATEGORY).active(panel.editor::isFlightMode);
        this.easing.keys().register(IKey.lang("aperture.gui.panels.keys.graph_easing"), Keyboard.KEY_RBRACKET, this::toggleEasing).held(Keyboard.KEY_LCONTROL).category(GuiAbstractFixturePanel.CATEGORY).active(panel.editor::isFlightMode);
    }

    @Override
    protected void toggleInterpolation()
    {
        super.toggleInterpolation();
        this.panel.editor.updateProfile();
    }

    @Override
    protected void doubleClick(int mouseX, int mouseY)
    {
        super.doubleClick(mouseX, mouseY);
        this.panel.editor.updateProfile();
    }

    @Override
    public void setTick(long value)
    {
        super.setTick(value);
        this.panel.editor.updateProfile();
    }

    @Override
    public void setValue(double value)
    {
        super.setValue(value);
        this.panel.editor.updateProfile();
    }

    @Override
    public void changeEasing()
    {
        super.changeEasing();
        this.panel.editor.updateProfile();
    }

    @Override
    public void pickInterpolation(KeyframeInterpolation interp)
    {
        super.pickInterpolation(interp);
        this.panel.editor.updateProfile();
    }
}