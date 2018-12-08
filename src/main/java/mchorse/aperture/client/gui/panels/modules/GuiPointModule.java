package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.data.Point;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Point GUI module
 *
 * This class unifies three trackpads into one object which edits a {@link Point},
 * and makes it way easier to reuse in other classes.
 */
public class GuiPointModule extends GuiAbstractModule
{
    public GuiTrackpadElement x;
    public GuiTrackpadElement y;
    public GuiTrackpadElement z;

    public Point point;

    public GuiPointModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.x = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.x"), (value) ->
        {
            this.point.x = value;
            this.editor.updateProfile();
        });

        this.y = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.y"), (value) ->
        {
            this.point.y = value;
            this.editor.updateProfile();
        });

        this.z = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.z"), (value) ->
        {
            this.point.z = value;
            this.editor.updateProfile();
        });

        this.x.resizer().parent(this.area).set(0, 0, 0, 20).w(1, 0);
        this.y.resizer().parent(this.area).set(0, 30, 0, 20).w(1, 0);
        this.z.resizer().parent(this.area).set(0, 60, 0, 20).w(1, 0);

        this.x.trackpad.amplitude = this.y.trackpad.amplitude = this.z.trackpad.amplitude = 0.1F;

        this.children.add(this.x, this.y, this.z);
    }

    public void fill(Point point)
    {
        this.point = point;

        this.x.setValue(point.x);
        this.y.setValue(point.y);
        this.z.setValue(point.z);
    }
}