package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Interpolation;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.widgets.buttons.GuiCirculate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public abstract class GuiKeyframesEditor<T extends GuiKeyframeElement> extends GuiElement
{
    public GuiElements<GuiElement> frameButtons;
    public GuiTrackpadElement tick;
    public GuiTrackpadElement value;
    public GuiButtonElement<GuiButton> interp;
    public GuiListElement<Interpolation> interpolations;
    public GuiButtonElement<GuiCirculate> easing;

    public T graph;

    private int clicks;
    private long clickTimer;

    public GuiKeyframesEditor(Minecraft mc)
    {
        super(mc);

        /* Create all elements */
        this.createChildren();

        this.frameButtons = new GuiElements<GuiElement>();
        this.frameButtons.setVisible(false);
        this.tick = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.tick"), (value) -> this.setTick(value.longValue()));
        this.tick.setLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        this.value = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.value"), (value) -> this.setValue(value))
        {
            @Override
            public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
            {
                return super.mouseClicked(mouseX, mouseY, mouseButton) || (this.isVisible() && this.area.isInside(mouseX, mouseY));
            }
        };

        this.interp = GuiButtonElement.button(mc, "", (b) -> this.interpolations.toggleVisible());
        this.interpolations = new GuiInterpolationsList(mc, (interp) -> this.pickInterpolation(interp));

        for (Interpolation interp : Interpolation.values())
        {
            this.interpolations.add(interp);
        }

        this.easing = new GuiButtonElement<GuiCirculate>(mc, new GuiCirculate(0, 0, 0, 80, 20), (b) -> this.changeEasing());
        this.easing.button.addLabel(I18n.format("aperture.gui.panels.easing.in"));
        this.easing.button.addLabel(I18n.format("aperture.gui.panels.easing.out"));
        this.easing.button.addLabel(I18n.format("aperture.gui.panels.easing.inout"));

        this.graph = this.createElement(mc);

        /* Position the elements */
        this.tick.resizer().parent(this.area).set(0, 10, 80, 20).x(1, -90);
        this.value.resizer().parent(this.area).set(0, 35, 80, 20).x(1, -90);

        this.interp.resizer().relative(this.tick.resizer()).set(-90, 0, 80, 20);
        this.easing.resizer().relative(this.value.resizer()).set(-90, 0, 80, 20);
        this.interpolations.resizer().parent(this.area).set(0, 30, 80, 20).x(1, -180).h(1, -60).maxH(16 * 7);
        this.graph.resizer().parent(this.area).set(0, 0, 0, 0).w(1, 0).h(1, 0);

        /* Add all elements */
        this.children.add(this.graph, this.frameButtons);
        this.frameButtons.add(this.interp, this.easing, this.tick, this.value, this.interpolations);
    }

    protected abstract T createElement(Minecraft mc);

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (super.mouseClicked(mouseX, mouseY, mouseButton))
        {
            return true;
        }

        if (this.area.isInside(mouseX, mouseY))
        {
            /* On double click add or remove a keyframe */
            if (mouseButton == 0)
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
    public boolean mouseScrolled(int mouseX, int mouseY, int scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll) || this.area.isInside(mouseX, mouseY);
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

    public void pickInterpolation(Interpolation interp)
    {
        if (this.graph.getCurrent() == null)
        {
            return;
        }

        this.graph.getCurrent().setInterpolation(interp);
        this.interp.button.displayString = I18n.format("aperture.gui.panels.interps." + interp.key);
        this.interpolations.setVisible(false);
    }

    public void changeEasing()
    {
        if (this.graph.getCurrent() == null)
        {
            return;
        }

        this.graph.getCurrent().setEasing(Easing.values()[this.easing.button.getValue()]);
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
        this.interp.button.displayString = I18n.format("aperture.gui.panels.interps." + frame.interp.key);
        this.interpolations.setCurrent(frame.interp);
        this.easing.button.setValue(frame.easing.ordinal());
    }
}