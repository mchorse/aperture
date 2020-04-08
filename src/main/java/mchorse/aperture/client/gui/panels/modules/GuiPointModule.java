package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.data.Point;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
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

        this.x = new GuiTrackpadElement(mc, (value) ->
        {
            this.point.x = value;
            this.editor.updateProfile();
        });
        this.x.tooltip(I18n.format("aperture.gui.panels.x"));

        this.y = new GuiTrackpadElement(mc, (value) ->
        {
            this.point.y = value;
            this.editor.updateProfile();
        });
        this.y.tooltip(I18n.format("aperture.gui.panels.y"));

        this.z = new GuiTrackpadElement(mc, (value) ->
        {
            this.point.z = value;
            this.editor.updateProfile();
        });
        this.z.tooltip(I18n.format("aperture.gui.panels.z"));

        this.x.flex().relative(this.area).set(0, 0, 0, 20).w(1, 0);
        this.y.flex().relative(this.area).set(0, 30, 0, 20).w(1, 0);
        this.z.flex().relative(this.area).set(0, 60, 0, 20).w(1, 0);

        this.x.values(0.1F);
        this.y.values(0.1F);
        this.z.values(0.1F);

        this.add(this.x, this.y, this.z);
    }

    public void fill(Point point)
    {
        this.point = point;

        this.x.setValue((float) point.x);
        this.y.setValue((float) point.y);
        this.z.setValue((float) point.z);
    }
}