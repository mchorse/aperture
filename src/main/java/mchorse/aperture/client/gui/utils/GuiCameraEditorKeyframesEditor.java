package mchorse.aperture.client.gui.utils;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.values.ValueKeyframeChannel;
import mchorse.aperture.camera.values.ValueProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.client.gui.utils.undo.FixtureValueChangeUndo;
import mchorse.aperture.utils.TimeUtils;
import mchorse.aperture.utils.undo.CompoundUndo;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiKeyframeElement;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiKeyframesEditor;
import mchorse.mclib.client.gui.framework.elements.keyframes.IAxisConverter;
import mchorse.mclib.client.gui.framework.elements.keyframes.Selection;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeInterpolation;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Special subclass of graph editor for fixture editor panels to allow 
 * dirtying the camera profile.
 */
public abstract class GuiCameraEditorKeyframesEditor<E extends GuiKeyframeElement> extends GuiKeyframesEditor<E>
{
    public static final AxisConverter CONVERTER = new AxisConverter();

    protected GuiCameraEditor editor;
    protected List<IConfigValue> valueChannels = new ArrayList<IConfigValue>();

    private List<Object> cachedData = new ArrayList<Object>();
    private int type = -1;
    private long lastUpdate;

    public GuiCameraEditorKeyframesEditor(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;

        this.interp.keys().register(IKey.lang("aperture.gui.panels.keys.graph_interp"), Keyboard.KEY_LBRACKET, this::toggleInterpolation).held(Keyboard.KEY_LCONTROL).category(GuiAbstractFixturePanel.CATEGORY).active(editor::isFlightDisabled);
        this.easing.keys().register(IKey.lang("aperture.gui.panels.keys.graph_easing"), Keyboard.KEY_RBRACKET, this::toggleEasing).held(Keyboard.KEY_LCONTROL).category(GuiAbstractFixturePanel.CATEGORY).active(editor::isFlightDisabled);
    }

    protected ValueKeyframeChannel get(IConfigValue value)
    {
        ValueKeyframeChannel keyframe = null;

        if (value instanceof ValueProxy && ((ValueProxy) value).getProxy() instanceof ValueKeyframeChannel)
        {
            keyframe = (ValueKeyframeChannel) ((ValueProxy) value).getProxy();
        }
        else if (value instanceof ValueKeyframeChannel)
        {
            keyframe = (ValueKeyframeChannel) value;
        }

        return keyframe;
    }

    public void updateConverter()
    {
        this.setConverter(CONVERTER);
    }

    public void markUndo(int type)
    {
        if (this.type == -1 || this.type == type)
        {
            this.lastUpdate = System.currentTimeMillis() + 400;
        }

        if (this.type != type)
        {
            if (this.type >= 0)
            {
                this.submitUndo();
            }

            this.cachedData.clear();

            for (IConfigValue channel : this.valueChannels)
            {
                this.cachedData.add(channel.getValue());
            }
        }

        this.type = type;
    }

    private void submitUndo()
    {
        this.type = -1;

        List<Object> newCachedData = new ArrayList<Object>();

        for (IConfigValue channel : this.valueChannels)
        {
            newCachedData.add(channel.getValue());
        }

        if (newCachedData.size() > 1)
        {
            FixtureValueChangeUndo[] undos = new FixtureValueChangeUndo[newCachedData.size()];

            for (int i = 0; i < undos.length; i++)
            {
                undos[i] = GuiAbstractFixturePanel.undo(this.editor, this.valueChannels.get(i).getId(), this.cachedData.get(i), newCachedData.get(i));
            }

            this.editor.postUndo(new CompoundUndo<CameraProfile>(undos).unmergable(), false);
        }
        else
        {
            IConfigValue channel = this.valueChannels.get(0);

            this.editor.postUndo(GuiAbstractFixturePanel.undo(this.editor, channel.getId(), this.cachedData.get(0), newCachedData.get(0)).unmergable(), false);
        }

        this.cachedData.clear();
    }

    @Override
    protected void doubleClick(int mouseX, int mouseY)
    {
        this.markUndo(0);
        super.doubleClick(mouseX, mouseY);
    }

    @Override
    public void removeSelectedKeyframes()
    {
        this.markUndo(1);
        super.removeSelectedKeyframes();
    }

    @Override
    public void setTick(double value)
    {
        this.markUndo(2);
        super.setTick(value);
    }

    @Override
    public void setValue(double value)
    {
        this.markUndo(3);
        super.setValue(value);
    }

    @Override
    public void changeEasing()
    {
        this.markUndo(4);
        super.changeEasing();
    }

    @Override
    public void pickInterpolation(KeyframeInterpolation interp)
    {
        this.markUndo(5);
        super.pickInterpolation(interp);
    }

    @Override
    public void draw(GuiContext context)
    {
        super.draw(context);

        if (this.type >= 0 && this.lastUpdate < System.currentTimeMillis())
        {
            this.submitUndo();
        }
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