package mchorse.aperture.client.gui.utils;

import mchorse.aperture.Aperture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.utils.TimeUtils;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiKeyframeElement;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiKeyframesEditor;
import mchorse.mclib.client.gui.framework.elements.keyframes.IAxisConverter;
import mchorse.mclib.client.gui.framework.elements.keyframes.Selection;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeInterpolation;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/**
 * Special subclass of graph editor for fixture editor panels to allow 
 * dirtying the camera profile.
 */
public abstract class GuiCameraEditorKeyframesEditor<E extends GuiKeyframeElement> extends GuiKeyframesEditor<E>
{
    public static final AxisConverter CONVERTER = new AxisConverter();

    protected GuiCameraEditor editor;

    public GuiCameraEditorKeyframesEditor(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;

        this.interp.keys().register(IKey.lang("aperture.gui.panels.keys.graph_interp"), Keyboard.KEY_LBRACKET, this::toggleInterpolation).held(Keyboard.KEY_LCONTROL).category(GuiAbstractFixturePanel.CATEGORY).active(editor::isFlightDisabled);
        this.easing.keys().register(IKey.lang("aperture.gui.panels.keys.graph_easing"), Keyboard.KEY_RBRACKET, this::toggleEasing).held(Keyboard.KEY_LCONTROL).category(GuiAbstractFixturePanel.CATEGORY).active(editor::isFlightDisabled);
    }

    public void updateConverter()
    {
        this.setConverter(CONVERTER);
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
    public void setTick(double value)
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

    public static class AxisConverter implements IAxisConverter
    {
        @Override
        public String format(double value)
        {
            return TimeUtils.formatTime((long) value);
        }

        @Override
        public double from(double v)
        {
            return TimeUtils.fromTime(v);
        }

        @Override
        public double to(double v)
        {
            return TimeUtils.toTime((long) v);
        }

        @Override
        public void updateField(GuiTrackpadElement element)
        {
            TimeUtils.configure(element, 0);

            element.limit(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        @Override
        public boolean forceInteger(Keyframe keyframe, Selection selection, boolean forceInteger)
        {
            return !Aperture.editorSeconds.get() && forceInteger;
        }
    }
}