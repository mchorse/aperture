package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.DragModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiDragModifierPanel extends GuiAbstractModifierPanel<DragModifier>
{
    public GuiTrackpadElement factor;
    public GuiActiveWidget active;

    public GuiDragModifierPanel(Minecraft mc, DragModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.factor = new GuiTrackpadElement(mc, I18n.format("aperture.gui.modifiers.panels.factor"), (value) ->
        {
            this.modifier.factor = value;
            this.modifiers.editor.updateProfile();
        });
        this.factor.setLimit(0, 1);
        this.factor.trackpad.amplitude = 0.05F;

        this.active = new GuiActiveWidget(mc, (value) ->
        {
            this.modifier.active = value;
            this.modifiers.editor.updateProfile();
        });

        this.factor.resizer().parent(this.area).set(5, 25, 0, 20).w(1, -10);
        this.active.resizer().parent(this.area).set(5, 50, 0, 20).w(1, -10);

        this.children.add(this.factor, this.active);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.factor.setValue(this.modifier.factor);
        this.active.value = this.modifier.active;
    }

    @Override
    public int getHeight()
    {
        return 75;
    }
}