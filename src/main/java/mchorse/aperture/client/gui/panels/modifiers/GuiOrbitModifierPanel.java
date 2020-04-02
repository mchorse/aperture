package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.OrbitModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiOrbitModifierPanel extends GuiAbstractModifierPanel<OrbitModifier>
{
    public GuiTrackpadElement yaw;
    public GuiTrackpadElement pitch;
    public GuiTrackpadElement distance;

    public GuiToggleElement copy;
    public GuiTextElement selector;

    public GuiOrbitModifierPanel(Minecraft mc, OrbitModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.yaw = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.yaw = value;
            this.modifiers.editor.updateProfile();
        });
        this.yaw.tooltip(I18n.format("aperture.gui.panels.yaw"));

        this.pitch = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.pitch = value;
            this.modifiers.editor.updateProfile();
        });
        this.pitch.tooltip(I18n.format("aperture.gui.panels.pitch"));

        this.distance = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.distance = value;
            this.modifiers.editor.updateProfile();
        });
        this.distance.tooltip(I18n.format("aperture.gui.panels.distance"));

        this.copy = new GuiToggleElement(mc, I18n.format("aperture.gui.modifiers.panels.copy_entity"), false, (b) ->
        {
            this.modifier.copy = b.isToggled();
            this.modifiers.editor.updateProfile();
        });

        this.selector = new GuiTextElement(mc, 500, (str) ->
        {
            this.modifier.selector = str;
            this.modifier.tryFindingEntity();
            this.modifiers.editor.updateProfile();
        });
        this.selector.tooltip(I18n.format("aperture.gui.panels.selector"));

        this.fields.add(this.selector, Elements.row(mc, 5, 0, 20, this.yaw, this.pitch), Elements.row(mc, 5, 0, 20, this.distance, this.copy));
    }

    @Override
    public void resize()
    {
        super.resize();

        this.yaw.setValue(this.modifier.yaw);
        this.pitch.setValue(this.modifier.pitch);
        this.distance.setValue(this.modifier.distance);

        this.copy.toggled(this.modifier.copy);
        this.selector.setText(this.modifier.selector);
    }

    @Override
    public void draw(GuiContext context)
    {
        super.draw(context);
    }
}