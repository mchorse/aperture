package mchorse.aperture.client.gui.utils;

import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Interpolation;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.client.gui.panels.GuiKeyframeFixturePanel.GuiInterpolationsList;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.widgets.buttons.GuiCirculate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiGraphEditor extends GuiElement
{
    public GuiElements<GuiElement> frameButtons;
    public GuiTrackpadElement tick;
    public GuiTrackpadElement value;
    public GuiButtonElement<GuiButton> interp;
    public GuiListElement<Interpolation> interpolations;
    public GuiButtonElement<GuiCirculate> easing;

    public GuiGraphElement graph;

    public GuiButtonElement<GuiButton> add;
    public GuiButtonElement<GuiButton> remove;

    public GuiGraphEditor(Minecraft mc)
    {
        super(mc);

        /* Create all elements */
        this.createChildren();

        this.frameButtons = new GuiElements<GuiElement>();
        this.tick = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.tick"), (value) ->
        {
            this.graph.getCurrent().setTick(value.longValue());
            this.graph.sliding = true;
        });
        this.tick.setLimit(0, Integer.MAX_VALUE, true);
        this.value = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.value"), (value) ->
        {
            this.graph.getCurrent().setValue(value);
        });

        this.add = GuiButtonElement.button(mc, I18n.format("aperture.gui.add"), (b) -> this.addKeyframe());
        this.remove = GuiButtonElement.button(mc, I18n.format("aperture.gui.remove"), (b) -> this.removeKeyframe());

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

        this.graph = new GuiGraphElement(mc, (frame) -> this.fillData(frame));

        /* Position the elmenents */
        this.tick.resizer().parent(this.area).set(0, 10, 80, 20).x(1, -90);
        this.value.resizer().parent(this.area).set(0, 35, 80, 20).x(1, -90);

        this.add.resizer().parent(this.area).set(0, 0, 30, 20).y(1, -30).x(1, -95);
        this.remove.resizer().parent(this.area).set(0, 0, 50, 20).y(1, -30).x(1, -60);
        this.interp.resizer().relative(this.tick.resizer()).set(-90, 0, 80, 20);
        this.easing.resizer().relative(this.value.resizer()).set(-90, 0, 80, 20);
        this.interpolations.resizer().relative(this.interp.resizer()).set(0, 20, 80, 16 * 5);
        this.graph.resizer().parent(this.area).set(0, 0, 0, 0).w(1, 0).h(1, 0);

        /* Add all elements */
        this.children.add(this.graph, this.add, this.remove, this.frameButtons);
        this.frameButtons.add(this.interp, this.easing, this.tick, this.value, this.interpolations);
    }

    public void addKeyframe()
    {
        Easing easing = Easing.IN;
        Interpolation interp = Interpolation.LINEAR;
        Keyframe frame = this.graph.getCurrent();
        long tick = this.getTick();
        long oldTick = tick;

        if (frame != null)
        {
            easing = frame.easing;
            interp = frame.interp;
            oldTick = frame.tick;
        }

        this.graph.selected = this.graph.channel.insert(tick, this.getValue());

        if (oldTick != tick)
        {
            frame = this.graph.getCurrent();
            frame.setEasing(easing);
            frame.setInterpolation(interp);
        }

        this.fillData(frame);
    }

    protected long getTick()
    {
        return this.graph.getOffset();
    }

    protected float getValue()
    {
        return 1;
    }

    public void removeKeyframe()
    {
        Keyframe frame = this.graph.getCurrent();

        if (frame == null)
        {
            return;
        }

        this.graph.channel.remove(this.graph.selected);
        this.graph.selected -= 1;
        this.fillData(this.graph.getCurrent());
    }

    public void pickInterpolation(Interpolation interp)
    {
        this.graph.getCurrent().setInterpolation(interp);
        this.interp.button.displayString = I18n.format("aperture.gui.panels.interps." + interp.key);
        this.interpolations.setVisible(false);
    }

    public void changeEasing()
    {
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