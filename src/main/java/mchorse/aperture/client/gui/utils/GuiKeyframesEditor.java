package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeInterpolation;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiCirculateElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public abstract class GuiKeyframesEditor<T extends GuiKeyframeElement> extends GuiElement
{
    public GuiElement frameButtons;
    public GuiTrackpadElement tick;
    public GuiTrackpadElement value;
    public GuiButtonElement interp;
    public GuiListElement<KeyframeInterpolation> interpolations;
    public GuiCirculateElement easing;

    public T graph;

    private int clicks;
    private long clickTimer;

    public GuiKeyframesEditor(Minecraft mc)
    {
        super(mc);

        this.frameButtons = new GuiElement(mc);
        this.frameButtons.setVisible(false);
        this.tick = new GuiTrackpadElement(mc, (value) -> this.setTick(value.longValue()));
        this.tick.limit(Integer.MIN_VALUE, Integer.MAX_VALUE, true).tooltip(IKey.lang("aperture.gui.panels.tick"));
        this.value = new GuiTrackpadElement(mc, (value) -> this.setValue(value));
        this.value.tooltip(IKey.lang("aperture.gui.panels.value"));
        this.interp = new GuiButtonElement(mc, IKey.lang(""), (b) -> this.interpolations.toggleVisible());
        this.interpolations = new GuiKeyframeInterpolationsList(mc, (interp) -> this.pickInterpolation(interp.get(0)));

        this.easing = new GuiCirculateElement(mc, (b) -> this.changeEasing());
        this.easing.addLabel(IKey.lang("aperture.gui.panels.easing.in"));
        this.easing.addLabel(IKey.lang("aperture.gui.panels.easing.out"));
        this.easing.addLabel(IKey.lang("aperture.gui.panels.easing.inout"));

        this.graph = this.createElement(mc);

        /* Position the elements */
        this.tick.flex().relative(this).set(0, 10, 80, 20).x(1, -90);
        this.value.flex().relative(this).set(0, 35, 80, 20).x(1, -90);

        this.interp.flex().relative(this.tick).set(-90, 0, 80, 20);
        this.easing.flex().relative(this.value).set(-90, 0, 80, 20);
        this.interpolations.flex().relative(this).set(0, 30, 80, 20).x(1, -180).h(1, -60).maxH(16 * 7);
        this.graph.flex().relative(this).set(0, 0, 0, 0).w(1, 0).h(1, 0);

        /* Add all elements */
        this.add(this.graph, this.frameButtons);
        this.frameButtons.add(this.tick, this.value, this.interp, this.easing, this.interpolations);
    }

    protected abstract T createElement(Minecraft mc);

    @Override
    public boolean mouseClicked(GuiContext context)
    {
        if (super.mouseClicked(context))
        {
            return true;
        }

        int mouseX = context.mouseX;
        int mouseY = context.mouseY;

        if (this.area.isInside(mouseX, mouseY))
        {
            /* On double click add or remove a keyframe */
            if (context.mouseButton == 0)
            {
                long time = System.currentTimeMillis();

                if (time - this.clickTimer < 175)
                {
                    this.clicks++;

                    if (this.clicks >= 1)
                    {
                        this.clicks = 0;
                        this.doubleClick(mouseX, mouseY);
                    }
                }
                else
                {
                    this.clicks = 0;
                }

                this.clickTimer = time;
            }
        }

        return this.area.isInside(mouseX, mouseY);
    }

    protected void doubleClick(int mouseX, int mouseY)
    {
        this.graph.doubleClick(mouseX, mouseY);
        this.fillData(this.graph.getCurrent());
    }

    @Override
    public boolean mouseScrolled(GuiContext context)
    {
        return super.mouseScrolled(context) || this.area.isInside(context.mouseX, context.mouseY);
    }

    public void setTick(long value)
    {
        this.graph.getCurrent().setTick(value);
        this.graph.setSliding();
    }

    public void setValue(float value)
    {
        this.graph.getCurrent().setValue(value);
    }

    public void pickInterpolation(KeyframeInterpolation interp)
    {
        if (this.graph.getCurrent() == null)
        {
            return;
        }

        this.graph.getCurrent().setInterpolation(interp);
        this.interp.label.set(interp.getKey());
        this.interpolations.setVisible(false);
    }

    public void changeEasing()
    {
        if (this.graph.getCurrent() == null)
        {
            return;
        }

        this.graph.getCurrent().setEasing(Easing.values()[this.easing.getValue()]);
    }

    public void fillData(Keyframe frame)
    {
        this.frameButtons.setVisible(frame != null);

        if (frame == null)
        {
            return;
        }

        this.tick.setValue(frame.tick);
        this.value.setValue(frame.value);
        this.interp.label.set(frame.interp.getKey());
        this.interpolations.setCurrent(frame.interp);
        this.easing.setValue(frame.easing.ordinal());
    }
}