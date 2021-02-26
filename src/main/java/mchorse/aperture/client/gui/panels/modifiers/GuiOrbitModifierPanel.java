package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.OrbitModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiTextHelpElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiOrbitModifierPanel extends GuiAbstractModifierPanel<OrbitModifier>
{
    public GuiTrackpadElement yaw;
    public GuiTrackpadElement pitch;
    public GuiTrackpadElement distance;

    public GuiToggleElement copy;
    public GuiTextHelpElement selector;

    public GuiOrbitModifierPanel(Minecraft mc, OrbitModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.yaw = new GuiTrackpadElement(mc, (value) -> this.modifiers.editor.postUndo(this.undo(this.modifier.yaw, value.floatValue())));
        this.yaw.tooltip(IKey.lang("aperture.gui.panels.yaw"));

        this.pitch = new GuiTrackpadElement(mc, (value) -> this.modifiers.editor.postUndo(this.undo(this.modifier.pitch, value.floatValue())));
        this.pitch.tooltip(IKey.lang("aperture.gui.panels.pitch"));

        this.distance = new GuiTrackpadElement(mc, (value) -> this.modifiers.editor.postUndo(this.undo(this.modifier.distance, value.floatValue())));
        this.distance.tooltip(IKey.lang("aperture.gui.panels.distance"));

        this.copy = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.copy_entity"), false, (b) -> this.modifiers.editor.postUndo(this.undo(this.modifier.copy, b.isToggled())));
        this.copy.flex().h(20);
        this.copy.tooltip(IKey.lang("aperture.gui.modifiers.panels.copy_entity_tooltip"));

        this.selector = new GuiTextHelpElement(mc, 500, (str) ->
        {
            this.modifiers.editor.postUndo(this.undo(this.modifier.selector, str));
            this.modifier.tryFindingEntity();
        });
        this.selector.link(GuiLookModifierPanel.TARGET_SELECTOR_HELP).tooltip(IKey.lang("aperture.gui.panels.selector"));

        this.fields.add(this.selector, Elements.row(mc, 5, 0, 20, this.yaw, this.pitch), Elements.row(mc, 5, 0, 20, this.distance, this.copy));
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.yaw.setValue(this.modifier.yaw.get());
        this.pitch.setValue(this.modifier.pitch.get());
        this.distance.setValue(this.modifier.distance.get());

        this.copy.toggled(this.modifier.copy.get());
        this.selector.setText(this.modifier.selector.get());
    }
}