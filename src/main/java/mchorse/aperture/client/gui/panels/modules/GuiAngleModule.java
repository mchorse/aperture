package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Angle GUI module
 *
 * This class unifies four trackpads into one object which edits a {@link Angle},
 * and makes it way easier to reuse in other classes.
 */
public class GuiAngleModule extends GuiAbstractModule
{
    public GuiTrackpadElement yaw;
    public GuiTrackpadElement pitch;
    public GuiTrackpadElement roll;
    public GuiTrackpadElement fov;

    public Angle angle;

    public GuiAngleModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.yaw = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.yaw"), (value) ->
        {
            this.angle.yaw = value;
            this.editor.updateProfile();
        });

        this.pitch = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.pitch"), (value) ->
        {
            this.angle.pitch = value;
            this.editor.updateProfile();
        });

        this.roll = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.roll"), (value) ->
        {
            this.angle.roll = value;
            this.editor.updateProfile();
        });

        this.fov = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.fov"), (value) ->
        {
            this.angle.fov = value;
            this.editor.updateProfile();
        });

        this.yaw.resizer().parent(this.area).set(0, 0, 0, 20).w(1, 0);
        this.pitch.resizer().parent(this.area).set(0, 20, 0, 20).w(1, 0);
        this.roll.resizer().parent(this.area).set(0, 40, 0, 20).w(1, 0);
        this.fov.resizer().parent(this.area).set(0, 60, 0, 20).w(1, 0);

        this.children.add(this.yaw, this.pitch, this.roll, this.fov);
    }

    public void fill(Angle angle)
    {
        this.angle = angle;

        this.yaw.setValue(angle.yaw);
        this.pitch.setValue(angle.pitch);
        this.roll.setValue(angle.roll);
        this.fov.setValue(angle.fov);
    }
}