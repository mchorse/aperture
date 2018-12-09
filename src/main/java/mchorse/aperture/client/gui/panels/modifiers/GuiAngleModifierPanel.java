package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.AngleModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiAngleModifierPanel extends GuiAbstractModifierPanel<AngleModifier>
{
    public GuiTrackpadElement yaw;
    public GuiTrackpadElement pitch;
    public GuiTrackpadElement roll;
    public GuiTrackpadElement fov;

    public GuiAngleModifierPanel(Minecraft mc, AngleModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.yaw = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.yaw"), (value) ->
        {
            this.modifier.angle.yaw = value;
            this.modifiers.editor.updateProfile();
        });

        this.pitch = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.pitch"), (value) ->
        {
            this.modifier.angle.pitch = value;
            this.modifiers.editor.updateProfile();
        });

        this.roll = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.roll"), (value) ->
        {
            this.modifier.angle.roll = value;
            this.modifiers.editor.updateProfile();
        });

        this.fov = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.fov"), (value) ->
        {
            this.modifier.angle.fov = value;
            this.modifiers.editor.updateProfile();
        });

        this.yaw.resizer().parent(this.area).set(5, 25, 0, 20).w(0.5F, -10);
        this.pitch.resizer().parent(this.area).set(5, 25, 0, 20).x(0.5F, 5).w(0.5F, -10);
        this.roll.resizer().parent(this.area).set(5, 50, 0, 20).w(0.5F, -10);
        this.fov.resizer().parent(this.area).set(5, 50, 0, 20).x(0.5F, 5).w(0.5F, -10);

        this.children.add(this.yaw, this.pitch, this.roll, this.fov);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.yaw.setValue(this.modifier.angle.yaw);
        this.pitch.setValue(this.modifier.angle.pitch);
        this.roll.setValue(this.modifier.angle.roll);
        this.fov.setValue(this.modifier.angle.fov);
    }

    @Override
    public int getHeight()
    {
        return 75;
    }
}