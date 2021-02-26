package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.values.ValueAngle;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.undo.FixtureValueChangeUndo;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

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

    public ValueAngle angle;

    public GuiAngleModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.yaw = new GuiTrackpadElement(mc, (value) ->
        {
            Angle point = this.angle.get().copy();

            point.yaw = value.floatValue();
            this.editor.postUndo(FixtureValueChangeUndo.create(this.editor, this.angle, point));
        });
        this.yaw.tooltip(IKey.lang("aperture.gui.panels.yaw"));

        this.pitch = new GuiTrackpadElement(mc, (value) ->
        {
            Angle point = this.angle.get().copy();

            point.pitch = value.floatValue();
            this.editor.postUndo(FixtureValueChangeUndo.create(this.editor, this.angle, point));
        });
        this.pitch.tooltip(IKey.lang("aperture.gui.panels.pitch"));

        this.roll = new GuiTrackpadElement(mc, (value) ->
        {
            Angle point = this.angle.get().copy();

            point.roll = value.floatValue();
            this.editor.postUndo(FixtureValueChangeUndo.create(this.editor, this.angle, point));
        });
        this.roll.tooltip(IKey.lang("aperture.gui.panels.roll"));

        this.fov = new GuiTrackpadElement(mc, (value) ->
        {
            Angle point = this.angle.get().copy();

            point.fov = value.floatValue();
            this.editor.postUndo(FixtureValueChangeUndo.create(this.editor, this.angle, point));
        });
        this.fov.tooltip(IKey.lang("aperture.gui.panels.fov"));

        this.flex().column(5).vertical().stretch().height(20);
        this.add(Elements.label(IKey.lang("aperture.gui.panels.angle")).background(0x88000000), this.yaw, this.pitch, this.roll, this.fov);
    }

    public void fill(ValueAngle angle)
    {
        this.angle = angle;

        this.yaw.setValue(angle.get().yaw);
        this.pitch.setValue(angle.get().pitch);
        this.roll.setValue(angle.get().roll);
        this.fov.setValue(angle.get().fov);
    }
}