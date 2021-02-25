package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.AngleModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiAngleModifierPanel extends GuiAbstractModifierPanel<AngleModifier>
{
    public GuiTrackpadElement yaw;
    public GuiTrackpadElement pitch;
    public GuiTrackpadElement roll;
    public GuiTrackpadElement fov;

    public GuiAngleModifierPanel(Minecraft mc, AngleModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.yaw = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.angle.get().yaw = value.floatValue();
            this.modifiers.editor.updateProfile();
        });
        this.yaw.tooltip(IKey.lang("aperture.gui.panels.yaw"));

        this.pitch = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.angle.get().pitch = value.floatValue();
            this.modifiers.editor.updateProfile();
        });
        this.pitch.tooltip(IKey.lang("aperture.gui.panels.pitch"));

        this.roll = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.angle.get().roll = value.floatValue();
            this.modifiers.editor.updateProfile();
        });
        this.roll.tooltip(IKey.lang("aperture.gui.panels.roll"));

        this.fov = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.angle.get().fov = value.floatValue();
            this.modifiers.editor.updateProfile();
        });
        this.fov.tooltip(IKey.lang("aperture.gui.panels.fov"));

        this.fields.add(Elements.row(mc, 5, 0, 20, this.yaw, this.pitch), Elements.row(mc, 5, 0, 20, this.roll, this.fov));
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.yaw.setValue(this.modifier.angle.get().yaw);
        this.pitch.setValue(this.modifier.angle.get().pitch);
        this.roll.setValue(this.modifier.angle.get().roll);
        this.fov.setValue(this.modifier.angle.get().fov);
    }
}