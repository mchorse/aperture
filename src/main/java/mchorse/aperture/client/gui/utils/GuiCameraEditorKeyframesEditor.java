package mchorse.aperture.client.gui.utils;

import mchorse.aperture.client.gui.GuiCameraEditor;
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
public abstract class GuiCameraEditorKeyframesEditor<E extends GuiKeyframeElement> extends GuiKeyframesEditor<E>
{
    protected GuiCameraEditor editor;

    public GuiCameraEditorKeyframesEditor(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;

        this.interp.keys().register(IKey.lang("aperture.gui.panels.keys.graph_interp"), Keyboard.KEY_LBRACKET, this::toggleInterpolation).held(Keyboard.KEY_LCONTROL).category(GuiAbstractFixturePanel.CATEGORY).active(editor::isFlightMode);
        this.easing.keys().register(IKey.lang("aperture.gui.panels.keys.graph_easing"), Keyboard.KEY_RBRACKET, this::toggleEasing).held(Keyboard.KEY_LCONTROL).category(GuiAbstractFixturePanel.CATEGORY).active(editor::isFlightMode);
    }

    @Override
    protected void toggleInterpolation()
    {
        super.toggleInterpolation();
        this.editor.updateProfile();
    }

    @Override
    protected void doubleClick(int mouseX, int mouseY)
    {
        super.doubleClick(mouseX, mouseY);
        this.editor.updateProfile();
    }

    @Override
    public void setTick(long value)
    {
        super.setTick(value);
        this.editor.updateProfile();
    }

    @Override
    public void setValue(double value)
    {
        super.setValue(value);
        this.editor.updateProfile();
    }

    @Override
    public void changeEasing()
    {
        super.changeEasing();
        this.editor.updateProfile();
    }

    @Override
    public void pickInterpolation(KeyframeInterpolation interp)
    {
        super.pickInterpolation(interp);
        this.editor.updateProfile();
    }
}